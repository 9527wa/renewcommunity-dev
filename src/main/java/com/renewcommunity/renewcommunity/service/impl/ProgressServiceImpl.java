package com.renewcommunity.renewcommunity.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysFile;
import com.renewcommunity.renewcommunity.entity.SysProgress;
import com.renewcommunity.renewcommunity.entity.SysProgressImage;
import com.renewcommunity.renewcommunity.entity.SysProject;
import com.renewcommunity.renewcommunity.entity.SysUser;
import com.renewcommunity.renewcommunity.mapper.SysFileMapper;
import com.renewcommunity.renewcommunity.mapper.SysProgressImageMapper;
import com.renewcommunity.renewcommunity.mapper.SysProgressMapper;
import com.renewcommunity.renewcommunity.mapper.SysProjectMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserMapper;
import com.renewcommunity.renewcommunity.model.dto.ProgressUpdateRequest;
import com.renewcommunity.renewcommunity.model.vo.ProgressImageVO;
import com.renewcommunity.renewcommunity.model.vo.ProgressVO;
import com.renewcommunity.renewcommunity.service.FileStorageService;
import com.renewcommunity.renewcommunity.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 工程进度实现。
 */
@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private static final String DEFAULT_STAGE = "施工阶段";

    private final SysProgressMapper progressMapper;

    private final SysProgressImageMapper imageMapper;

    private final SysFileMapper fileMapper;

    private final SysProjectMapper projectMapper;

    private final SysUserMapper userMapper;

    private final FileStorageService fileStorageService;

    @Override
    public PageResult<ProgressVO> listProgress(Long projectId, String stage, int pageNum, int pageSize) {
        Page<SysProgress> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 200));
        var query = Wrappers.<SysProgress>lambdaQuery();

        if (projectId != null) {
            query.eq(SysProgress::getProjectId, projectId);
        }

        if (StringUtils.hasText(stage)) {
            query.eq(SysProgress::getStage, stage);
        }

        query.orderByDesc(SysProgress::getReportDate).orderByDesc(SysProgress::getId);
        Page<SysProgress> result = progressMapper.selectPage(page, query);
        List<SysProgress> records = result.getRecords();

        List<Long> progressIds = records.stream().map(SysProgress::getId).toList();
        Map<Long, List<ProgressImageVO>> images = loadImages(progressIds);
        Map<Long, String> projectNames = projectNameMap(records.stream()
                .map(SysProgress::getProjectId)
                .filter(Objects::nonNull)
                .toList());

        List<ProgressVO> list = records.stream()
                .map(item -> new ProgressVO(item.getId(), item.getProjectId(), projectNames.get(item.getProjectId()),
                        item.getStage(), item.getTitle(), item.getContent(), item.getProgress(),
                        item.getReportDate(), item.getCreatorName(), item.getCreatedAt(),
                        images.getOrDefault(item.getId(), List.of())))
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    @Transactional
    public Long createProgress(Long projectId, String stage, String title, String content, Integer progress,
                               LocalDate reportDate, List<MultipartFile> files) {
        SysProject project = requireProject(projectId);

        if (!StringUtils.hasText(title)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "进度标题不能为空");
        }

        if (progress != null && (progress < 0 || progress > 100)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "进度需在 0-100 之间");
        }

        Long creatorId = StpUtil.getLoginIdAsLong();
        SysProgress record = new SysProgress();
        record.setProjectId(projectId);
        record.setStage(StringUtils.hasText(stage) ? stage : DEFAULT_STAGE);
        record.setTitle(title);
        record.setContent(content == null ? "" : content);
        record.setProgress(progress == null ? project.getProgress() : progress);
        record.setReportDate(reportDate == null ? LocalDate.now() : reportDate);
        record.setCreatorId(creatorId);
        record.setCreatorName(resolveUserName(creatorId));
        progressMapper.insert(record);

        saveImages(record.getId(), files, creatorId);
        syncProjectProgress(projectId, progress);

        return record.getId();
    }

    @Override
    @Transactional
    public void updateProgress(Long id, ProgressUpdateRequest request) {
        SysProgress record = requireProgress(id);

        SysProgress update = new SysProgress();
        update.setId(id);
        update.setStage(request.stage());
        update.setTitle(request.title());
        update.setContent(request.content() == null ? "" : request.content());
        update.setProgress(request.progress());
        update.setReportDate(request.reportDate());
        progressMapper.updateById(update);

        syncProjectProgress(record.getProjectId(), request.progress());
    }

    @Override
    @Transactional
    public void addImages(Long id, List<MultipartFile> files) {
        requireProgress(id);
        saveImages(id, files, StpUtil.getLoginIdAsLong());
    }

    @Override
    @Transactional
    public void deleteProgress(Long id) {
        requireProgress(id);

        List<SysProgressImage> images = imageMapper.selectList(Wrappers.<SysProgressImage>lambdaQuery()
                .eq(SysProgressImage::getProgressId, id));
        images.forEach(image -> fileStorageService.delete(fileMapper.selectById(image.getFileId())));

        imageMapper.delete(Wrappers.<SysProgressImage>lambdaQuery().eq(SysProgressImage::getProgressId, id));
        progressMapper.deleteById(id);
    }

    private void saveImages(Long progressId, List<MultipartFile> files, Long creatorId) {
        if (files == null || files.isEmpty()) {
            return;
        }

        String creatorName = resolveUserName(creatorId);
        int sort = 0;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            SysFile stored = fileStorageService.store(file, creatorId, creatorName);
            SysProgressImage image = new SysProgressImage();
            image.setProgressId(progressId);
            image.setFileId(stored.getId());
            image.setSort(sort++);
            imageMapper.insert(image);
        }
    }

    private void syncProjectProgress(Long projectId, Integer progress) {
        if (projectId == null || progress == null) {
            return;
        }

        SysProject update = new SysProject();
        update.setId(projectId);
        update.setProgress(progress);
        projectMapper.updateById(update);
    }

    private Map<Long, List<ProgressImageVO>> loadImages(Collection<Long> progressIds) {
        if (progressIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<SysProgressImage> images = imageMapper.selectList(Wrappers.<SysProgressImage>lambdaQuery()
                .in(SysProgressImage::getProgressId, progressIds)
                .orderByAsc(SysProgressImage::getSort));
        if (images.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> fileIds = images.stream().map(SysProgressImage::getFileId).toList();
        Map<Long, SysFile> files = fileMapper.selectBatchIds(fileIds).stream()
                .collect(Collectors.toMap(SysFile::getId, item -> item));

        return images.stream().collect(Collectors.groupingBy(SysProgressImage::getProgressId,
                Collectors.mapping(image -> {
                    SysFile file = files.get(image.getFileId());
                    return new ProgressImageVO(image.getId(), image.getFileId(),
                            file == null ? null : file.getOriginalName(),
                            file == null ? null : file.getSize(),
                            file == null ? null : file.getContentType());
                }, Collectors.toList())));
    }

    private Map<Long, String> projectNameMap(Collection<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return projectMapper.selectBatchIds(projectIds).stream()
                .collect(Collectors.toMap(SysProject::getId, SysProject::getName));
    }

    private SysProject requireProject(Long projectId) {
        SysProject project = projectId == null ? null : projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "关联项目不存在");
        }

        return project;
    }

    private SysProgress requireProgress(Long id) {
        SysProgress record = id == null ? null : progressMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "进度记录不存在");
        }

        return record;
    }

    private String resolveUserName(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return "用户" + userId;
        }

        return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
    }
}

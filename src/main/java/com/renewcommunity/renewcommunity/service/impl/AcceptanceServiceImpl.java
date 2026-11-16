package com.renewcommunity.renewcommunity.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysAcceptance;
import com.renewcommunity.renewcommunity.entity.SysAcceptanceFile;
import com.renewcommunity.renewcommunity.entity.SysFile;
import com.renewcommunity.renewcommunity.entity.SysProject;
import com.renewcommunity.renewcommunity.entity.SysUser;
import com.renewcommunity.renewcommunity.mapper.SysAcceptanceFileMapper;
import com.renewcommunity.renewcommunity.mapper.SysAcceptanceMapper;
import com.renewcommunity.renewcommunity.mapper.SysFileMapper;
import com.renewcommunity.renewcommunity.mapper.SysProjectMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserMapper;
import com.renewcommunity.renewcommunity.model.dto.AcceptanceUpdateRequest;
import com.renewcommunity.renewcommunity.model.vo.AcceptanceFileVO;
import com.renewcommunity.renewcommunity.model.vo.AcceptanceVO;
import com.renewcommunity.renewcommunity.service.AcceptanceService;
import com.renewcommunity.renewcommunity.service.FileStorageService;
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
 * 项目验收实现。
 */
@Service
@RequiredArgsConstructor
public class AcceptanceServiceImpl implements AcceptanceService {

    private final SysAcceptanceMapper acceptanceMapper;

    private final SysAcceptanceFileMapper acceptanceFileMapper;

    private final SysFileMapper fileMapper;

    private final SysProjectMapper projectMapper;

    private final SysUserMapper userMapper;

    private final FileStorageService fileStorageService;

    @Override
    public PageResult<AcceptanceVO> listAcceptances(Long projectId, Integer result, int pageNum, int pageSize) {
        Page<SysAcceptance> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 200));
        var query = Wrappers.<SysAcceptance>lambdaQuery();

        if (projectId != null) {
            query.eq(SysAcceptance::getProjectId, projectId);
        }

        if (result != null) {
            query.eq(SysAcceptance::getResult, result);
        }

        query.orderByDesc(SysAcceptance::getAcceptDate).orderByDesc(SysAcceptance::getId);
        Page<SysAcceptance> pageResult = acceptanceMapper.selectPage(page, query);
        List<SysAcceptance> records = pageResult.getRecords();

        Map<Long, List<AcceptanceFileVO>> attachments = loadAttachments(records.stream()
                .map(SysAcceptance::getId)
                .toList());
        Map<Long, String> projectNames = projectNameMap(records.stream()
                .map(SysAcceptance::getProjectId)
                .filter(Objects::nonNull)
                .toList());

        List<AcceptanceVO> list = records.stream()
                .map(item -> new AcceptanceVO(item.getId(), item.getProjectId(), projectNames.get(item.getProjectId()),
                        item.getTitle(), item.getResult(), item.getAcceptDate(), item.getConclusion(),
                        item.getCreatorName(), item.getCreatedAt(),
                        attachments.getOrDefault(item.getId(), List.of())))
                .toList();

        return new PageResult<>(pageResult.getTotal(), list);
    }

    @Override
    @Transactional
    public Long createAcceptance(Long projectId, String title, Integer result, LocalDate acceptDate, String conclusion,
                                List<MultipartFile> files) {
        requireProject(projectId);

        if (!StringUtils.hasText(title)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验收名称不能为空");
        }

        if (result != null && (result < 0 || result > 2)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "验收结果不合法");
        }

        Long creatorId = StpUtil.getLoginIdAsLong();
        SysAcceptance record = new SysAcceptance();
        record.setProjectId(projectId);
        record.setTitle(title);
        record.setResult(result == null ? 0 : result);
        record.setAcceptDate(acceptDate);
        record.setConclusion(conclusion == null ? "" : conclusion);
        record.setCreatorId(creatorId);
        record.setCreatorName(resolveUserName(creatorId));
        acceptanceMapper.insert(record);

        saveFiles(record.getId(), files, creatorId);

        if (Integer.valueOf(1).equals(record.getResult())) {
            markProjectCompleted(projectId);
        }

        return record.getId();
    }

    @Override
    @Transactional
    public void updateAcceptance(Long id, AcceptanceUpdateRequest request) {
        SysAcceptance record = requireAcceptance(id);

        SysAcceptance update = new SysAcceptance();
        update.setId(id);
        update.setTitle(request.title());
        update.setResult(request.result());
        update.setAcceptDate(request.acceptDate());
        update.setConclusion(request.conclusion() == null ? "" : request.conclusion());
        acceptanceMapper.updateById(update);

        if (Integer.valueOf(1).equals(request.result())) {
            markProjectCompleted(record.getProjectId());
        }
    }

    @Override
    @Transactional
    public void deleteAcceptance(Long id) {
        requireAcceptance(id);

        List<SysAcceptanceFile> files = acceptanceFileMapper.selectList(Wrappers.<SysAcceptanceFile>lambdaQuery()
                .eq(SysAcceptanceFile::getAcceptanceId, id));
        files.forEach(item -> fileStorageService.delete(fileMapper.selectById(item.getFileId())));

        acceptanceFileMapper.delete(Wrappers.<SysAcceptanceFile>lambdaQuery()
                .eq(SysAcceptanceFile::getAcceptanceId, id));
        acceptanceMapper.deleteById(id);
    }

    private void saveFiles(Long acceptanceId, List<MultipartFile> files, Long creatorId) {
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
            SysAcceptanceFile link = new SysAcceptanceFile();
            link.setAcceptanceId(acceptanceId);
            link.setFileId(stored.getId());
            link.setSort(sort++);
            acceptanceFileMapper.insert(link);
        }
    }

    private Map<Long, List<AcceptanceFileVO>> loadAttachments(Collection<Long> acceptanceIds) {
        if (acceptanceIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<SysAcceptanceFile> links = acceptanceFileMapper.selectList(Wrappers.<SysAcceptanceFile>lambdaQuery()
                .in(SysAcceptanceFile::getAcceptanceId, acceptanceIds)
                .orderByAsc(SysAcceptanceFile::getSort));
        if (links.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> fileIds = links.stream().map(SysAcceptanceFile::getFileId).toList();
        Map<Long, SysFile> files = fileMapper.selectBatchIds(fileIds).stream()
                .collect(Collectors.toMap(SysFile::getId, item -> item));

        return links.stream().collect(Collectors.groupingBy(SysAcceptanceFile::getAcceptanceId,
                Collectors.mapping(link -> {
                    SysFile file = files.get(link.getFileId());
                    return new AcceptanceFileVO(link.getId(), link.getFileId(),
                            file == null ? null : file.getOriginalName(),
                            file == null ? null : file.getSize(),
                            file == null ? null : file.getContentType());
                }, Collectors.toList())));
    }

    private void markProjectCompleted(Long projectId) {
        SysProject update = new SysProject();
        update.setId(projectId);
        update.setStatus(2);
        update.setProgress(100);
        projectMapper.updateById(update);
    }

    private Map<Long, String> projectNameMap(Collection<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return projectMapper.selectBatchIds(projectIds).stream()
                .collect(Collectors.toMap(SysProject::getId, SysProject::getName));
    }

    private void requireProject(Long projectId) {
        if (projectId == null || projectMapper.selectById(projectId) == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "关联项目不存在");
        }
    }

    private SysAcceptance requireAcceptance(Long id) {
        SysAcceptance record = id == null ? null : acceptanceMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "验收记录不存在");
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

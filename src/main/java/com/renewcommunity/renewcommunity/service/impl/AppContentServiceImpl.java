package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysAnnouncement;
import com.renewcommunity.renewcommunity.entity.SysCommunity;
import com.renewcommunity.renewcommunity.entity.SysFile;
import com.renewcommunity.renewcommunity.entity.SysProgress;
import com.renewcommunity.renewcommunity.entity.SysProgressImage;
import com.renewcommunity.renewcommunity.entity.SysProject;
import com.renewcommunity.renewcommunity.mapper.SysAnnouncementMapper;
import com.renewcommunity.renewcommunity.mapper.SysCommunityMapper;
import com.renewcommunity.renewcommunity.mapper.SysFileMapper;
import com.renewcommunity.renewcommunity.mapper.SysProgressImageMapper;
import com.renewcommunity.renewcommunity.mapper.SysProgressMapper;
import com.renewcommunity.renewcommunity.mapper.SysProjectMapper;
import com.renewcommunity.renewcommunity.model.vo.AppAnnouncementDetailVO;
import com.renewcommunity.renewcommunity.model.vo.AppAnnouncementVO;
import com.renewcommunity.renewcommunity.model.vo.AppCommunityVO;
import com.renewcommunity.renewcommunity.model.vo.AppProgressVO;
import com.renewcommunity.renewcommunity.model.vo.FileRefVO;
import com.renewcommunity.renewcommunity.model.vo.AppProjectVO;
import com.renewcommunity.renewcommunity.service.AppContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * C 端公开内容实现。
 */
@Service
@RequiredArgsConstructor
public class AppContentServiceImpl implements AppContentService {

    private static final int SUMMARY_LENGTH = 60;

    private final SysAnnouncementMapper announcementMapper;

    private final SysProjectMapper projectMapper;

    private final SysCommunityMapper communityMapper;

    private final SysProgressMapper progressMapper;

    private final SysProgressImageMapper progressImageMapper;

    private final SysFileMapper fileMapper;

    @Override
    public PageResult<AppAnnouncementVO> listAnnouncements(String keyword, int pageNum, int pageSize) {
        Page<SysAnnouncement> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 100));
        var query = Wrappers.<SysAnnouncement>lambdaQuery()
                .eq(SysAnnouncement::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like(SysAnnouncement::getTitle, keyword)
                    .or()
                    .like(SysAnnouncement::getContent, keyword));
        }

        query.orderByDesc(SysAnnouncement::getPublishedAt)
                .orderByDesc(SysAnnouncement::getId);

        Page<SysAnnouncement> result = announcementMapper.selectPage(page, query);
        List<AppAnnouncementVO> list = result.getRecords().stream()
                .map(item -> new AppAnnouncementVO(item.getId(), item.getTitle(), item.getType(),
                        summarize(item.getContent()), item.getPublishedAt()))
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    public AppAnnouncementDetailVO getAnnouncement(Long id) {
        SysAnnouncement announcement = id == null ? null : announcementMapper.selectById(id);

        if (announcement == null || !Integer.valueOf(1).equals(announcement.getStatus())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "公告不存在或已下架");
        }

        return new AppAnnouncementDetailVO(announcement.getId(), announcement.getTitle(),
                announcement.getContent(), announcement.getType(), announcement.getPublishedAt());
    }

    @Override
    public PageResult<AppProjectVO> listProjects(Integer status, String community, String keyword,
                                                 int pageNum, int pageSize) {
        Page<SysProject> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 100));
        var query = Wrappers.<SysProject>lambdaQuery();

        if (status != null) {
            query.eq(SysProject::getStatus, status);
        }

        if (StringUtils.hasText(community)) {
            query.eq(SysProject::getCommunity, community);
        }

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like(SysProject::getName, keyword)
                    .or()
                    .like(SysProject::getCommunity, keyword)
                    .or()
                    .like(SysProject::getCategory, keyword));
        }

        query.orderByDesc(SysProject::getId);
        Page<SysProject> result = projectMapper.selectPage(page, query);
        List<AppProjectVO> list = result.getRecords().stream().map(AppContentServiceImpl::toVO).toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    public AppProjectVO getProject(Long id) {
        SysProject project = id == null ? null : projectMapper.selectById(id);

        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }

        return toVO(project);
    }

    @Override
    public PageResult<AppProgressVO> listProjectProgress(Long projectId, int pageNum, int pageSize) {
        if (projectId == null || projectMapper.selectById(projectId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }

        Page<SysProgress> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 100));
        var query = Wrappers.<SysProgress>lambdaQuery()
                .eq(SysProgress::getProjectId, projectId)
                .orderByDesc(SysProgress::getReportDate)
                .orderByDesc(SysProgress::getId);
        Page<SysProgress> result = progressMapper.selectPage(page, query);
        Map<Long, List<FileRefVO>> images = loadProgressImages(result.getRecords().stream()
                .map(SysProgress::getId)
                .toList());

        List<AppProgressVO> list = result.getRecords().stream()
                .map(item -> new AppProgressVO(item.getId(), item.getStage(), item.getTitle(), item.getContent(),
                        item.getProgress(), item.getReportDate(), item.getCreatedAt(),
                        images.getOrDefault(item.getId(), List.of())))
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    public List<AppCommunityVO> listCommunities() {
        return communityMapper.selectList(Wrappers.<SysCommunity>lambdaQuery()
                        .eq(SysCommunity::getStatus, 1)
                        .orderByAsc(SysCommunity::getId))
                .stream()
                .map(item -> new AppCommunityVO(item.getId(), item.getName(), item.getAddress(),
                        item.getBuildingCount(), item.getHouseholdCount()))
                .toList();
    }

    private Map<Long, List<FileRefVO>> loadProgressImages(Collection<Long> progressIds) {
        if (progressIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<SysProgressImage> links = progressImageMapper.selectList(Wrappers.<SysProgressImage>lambdaQuery()
                .in(SysProgressImage::getProgressId, progressIds)
                .orderByAsc(SysProgressImage::getSort));
        if (links.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> fileIds = links.stream().map(SysProgressImage::getFileId).toList();
        Map<Long, SysFile> files = fileMapper.selectBatchIds(fileIds).stream()
                .collect(Collectors.toMap(SysFile::getId, item -> item));

        return links.stream().collect(Collectors.groupingBy(SysProgressImage::getProgressId,
                Collectors.mapping(link -> {
                    SysFile file = files.get(link.getFileId());
                    return new FileRefVO(link.getFileId(),
                            file == null ? null : file.getOriginalName(),
                            file == null ? null : file.getContentType());
                }, Collectors.toList())));
    }

    private String summarize(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }

        String text = content.replaceAll("\\s+", " ").trim();
        return text.length() > SUMMARY_LENGTH ? text.substring(0, SUMMARY_LENGTH) + "…" : text;
    }

    private static AppProjectVO toVO(SysProject project) {
        return new AppProjectVO(project.getId(), project.getName(), project.getCommunity(), project.getCategory(),
                project.getStatus(), project.getProgress(), project.getStartDate(), project.getEndDate(),
                project.getConstructionUnit(), project.getManager(), project.getDescription());
    }
}

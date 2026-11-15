package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysAnnouncement;
import com.renewcommunity.renewcommunity.entity.SysCommunity;
import com.renewcommunity.renewcommunity.entity.SysProject;
import com.renewcommunity.renewcommunity.mapper.SysAnnouncementMapper;
import com.renewcommunity.renewcommunity.mapper.SysCommunityMapper;
import com.renewcommunity.renewcommunity.mapper.SysProjectMapper;
import com.renewcommunity.renewcommunity.model.vo.AppAnnouncementDetailVO;
import com.renewcommunity.renewcommunity.model.vo.AppAnnouncementVO;
import com.renewcommunity.renewcommunity.model.vo.AppCommunityVO;
import com.renewcommunity.renewcommunity.model.vo.AppProjectVO;
import com.renewcommunity.renewcommunity.service.AppContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

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

    @Override
    public PageResult<AppAnnouncementVO> listAnnouncements(int pageNum, int pageSize) {
        Page<SysAnnouncement> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 100));
        var query = Wrappers.<SysAnnouncement>lambdaQuery()
                .eq(SysAnnouncement::getStatus, 1)
                .orderByDesc(SysAnnouncement::getPublishedAt)
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
    public PageResult<AppProjectVO> listProjects(Integer status, String community, int pageNum, int pageSize) {
        Page<SysProject> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 100));
        var query = Wrappers.<SysProject>lambdaQuery();

        if (status != null) {
            query.eq(SysProject::getStatus, status);
        }

        if (StringUtils.hasText(community)) {
            query.eq(SysProject::getCommunity, community);
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
    public List<AppCommunityVO> listCommunities() {
        return communityMapper.selectList(Wrappers.<SysCommunity>lambdaQuery()
                        .eq(SysCommunity::getStatus, 1)
                        .orderByAsc(SysCommunity::getId))
                .stream()
                .map(item -> new AppCommunityVO(item.getId(), item.getName(), item.getAddress(),
                        item.getBuildingCount(), item.getHouseholdCount()))
                .toList();
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
                project.getDescription());
    }
}

package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysAnnouncement;
import com.renewcommunity.renewcommunity.mapper.SysAnnouncementMapper;
import com.renewcommunity.renewcommunity.model.dto.AnnouncementSaveRequest;
import com.renewcommunity.renewcommunity.model.vo.AnnouncementVO;
import com.renewcommunity.renewcommunity.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告管理实现。
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private static final String DEFAULT_TYPE = "公告通知";

    private final SysAnnouncementMapper announcementMapper;

    @Override
    public PageResult<AnnouncementVO> listAnnouncements(String keyword, Integer status, int pageNum, int pageSize) {
        Page<SysAnnouncement> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 200));
        var query = Wrappers.<SysAnnouncement>lambdaQuery();

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like(SysAnnouncement::getTitle, keyword)
                    .or()
                    .like(SysAnnouncement::getContent, keyword));
        }

        if (status != null) {
            query.eq(SysAnnouncement::getStatus, status);
        }

        query.orderByDesc(SysAnnouncement::getCreatedAt).orderByDesc(SysAnnouncement::getId);
        Page<SysAnnouncement> result = announcementMapper.selectPage(page, query);
        List<AnnouncementVO> list = result.getRecords().stream().map(AnnouncementServiceImpl::toVO).toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    public Long createAnnouncement(AnnouncementSaveRequest request) {
        SysAnnouncement announcement = new SysAnnouncement();
        apply(announcement, request);
        announcement.setStatus(0);
        announcementMapper.insert(announcement);

        return announcement.getId();
    }

    @Override
    public void updateAnnouncement(Long id, AnnouncementSaveRequest request) {
        requireAnnouncement(id);

        SysAnnouncement announcement = new SysAnnouncement();
        announcement.setId(id);
        apply(announcement, request);
        announcementMapper.updateById(announcement);
    }

    @Override
    public void publishAnnouncement(Long id) {
        SysAnnouncement announcement = requireAnnouncement(id);

        SysAnnouncement update = new SysAnnouncement();
        update.setId(id);
        update.setStatus(1);
        if (announcement.getPublishedAt() == null) {
            update.setPublishedAt(LocalDateTime.now());
        }
        announcementMapper.updateById(update);
    }

    @Override
    public void offlineAnnouncement(Long id) {
        requireAnnouncement(id);

        SysAnnouncement update = new SysAnnouncement();
        update.setId(id);
        update.setStatus(2);
        announcementMapper.updateById(update);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        requireAnnouncement(id);
        announcementMapper.deleteById(id);
    }

    private void apply(SysAnnouncement announcement, AnnouncementSaveRequest request) {
        announcement.setTitle(request.title());
        announcement.setContent(request.content());
        announcement.setType(StringUtils.hasText(request.type()) ? request.type() : DEFAULT_TYPE);
    }

    private SysAnnouncement requireAnnouncement(Long id) {
        SysAnnouncement announcement = id == null ? null : announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "公告不存在");
        }

        return announcement;
    }

    private static AnnouncementVO toVO(SysAnnouncement announcement) {
        return new AnnouncementVO(announcement.getId(), announcement.getTitle(), announcement.getContent(),
                announcement.getType(), announcement.getStatus(), announcement.getPublishedAt(),
                announcement.getCreatedAt());
    }
}

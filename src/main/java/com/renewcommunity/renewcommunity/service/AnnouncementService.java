package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.dto.AnnouncementSaveRequest;
import com.renewcommunity.renewcommunity.model.vo.AnnouncementVO;

/**
 * 公告管理服务。
 */
public interface AnnouncementService {

    PageResult<AnnouncementVO> listAnnouncements(String keyword, Integer status, int pageNum, int pageSize);

    Long createAnnouncement(AnnouncementSaveRequest request);

    void updateAnnouncement(Long id, AnnouncementSaveRequest request);

    void publishAnnouncement(Long id);

    void offlineAnnouncement(Long id);

    void deleteAnnouncement(Long id);
}

package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.vo.AppAnnouncementDetailVO;
import com.renewcommunity.renewcommunity.model.vo.AppAnnouncementVO;
import com.renewcommunity.renewcommunity.model.vo.AppCommunityVO;
import com.renewcommunity.renewcommunity.model.vo.AppProgressVO;
import com.renewcommunity.renewcommunity.model.vo.AppProjectVO;

import java.util.List;

/**
 * C 端公开内容服务（公告、项目、小区）。
 */
public interface AppContentService {

    PageResult<AppAnnouncementVO> listAnnouncements(String keyword, int pageNum, int pageSize);

    AppAnnouncementDetailVO getAnnouncement(Long id);

    PageResult<AppProjectVO> listProjects(Integer status, String community, String keyword, int pageNum,
                                         int pageSize);

    AppProjectVO getProject(Long id);

    PageResult<AppProgressVO> listProjectProgress(Long projectId, int pageNum, int pageSize);

    List<AppCommunityVO> listCommunities();
}

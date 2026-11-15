package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.dto.CommunitySaveRequest;
import com.renewcommunity.renewcommunity.model.vo.CommunityVO;

import java.util.List;

/**
 * 小区管理服务。
 */
public interface CommunityService {

    PageResult<CommunityVO> listCommunities(String keyword, Integer status, int pageNum, int pageSize);

    List<CommunityVO> listAllCommunities();

    Long createCommunity(CommunitySaveRequest request);

    void updateCommunity(Long id, CommunitySaveRequest request);

    void deleteCommunity(Long id);
}

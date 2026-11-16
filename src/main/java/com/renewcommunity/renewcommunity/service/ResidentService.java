package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.vo.CommunityBindingVO;
import com.renewcommunity.renewcommunity.model.vo.ResidentVO;

import java.util.List;

/**
 * 居民用户管理服务。
 */
public interface ResidentService {

    PageResult<ResidentVO> listResidents(String keyword, Long communityId, int pageNum, int pageSize);

    void bindCommunity(Long userId, Long communityId);

    List<CommunityBindingVO> listBindings(Long userId);
}

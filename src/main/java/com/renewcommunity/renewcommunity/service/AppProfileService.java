package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.model.dto.AppProfileUpdateRequest;
import com.renewcommunity.renewcommunity.model.vo.AppProfileVO;

/**
 * C 端个人资料服务。
 */
public interface AppProfileService {

    AppProfileVO getProfile(Long userId);

    void updateProfile(Long userId, AppProfileUpdateRequest request);

    void bindCommunity(Long userId, Long communityId);
}

package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysCommunity;
import com.renewcommunity.renewcommunity.entity.SysComplaint;
import com.renewcommunity.renewcommunity.entity.SysRole;
import com.renewcommunity.renewcommunity.entity.SysSatisfaction;
import com.renewcommunity.renewcommunity.entity.SysUser;
import com.renewcommunity.renewcommunity.entity.SysUserCommunityLog;
import com.renewcommunity.renewcommunity.mapper.SysCommunityMapper;
import com.renewcommunity.renewcommunity.mapper.SysComplaintMapper;
import com.renewcommunity.renewcommunity.mapper.SysRoleMapper;
import com.renewcommunity.renewcommunity.mapper.SysSatisfactionMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserCommunityLogMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserMapper;
import com.renewcommunity.renewcommunity.model.dto.AppProfileUpdateRequest;
import com.renewcommunity.renewcommunity.model.vo.AppProfileVO;
import com.renewcommunity.renewcommunity.service.AppProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * C 端个人资料实现。
 */
@Service
@RequiredArgsConstructor
public class AppProfileServiceImpl implements AppProfileService {

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    private final SysCommunityMapper communityMapper;

    private final SysComplaintMapper complaintMapper;

    private final SysSatisfactionMapper satisfactionMapper;

    private final SysUserCommunityLogMapper bindingLogMapper;

    @Override
    public AppProfileVO getProfile(Long userId) {
        SysUser user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        List<String> roles = roleMapper.selectEnabledByUserId(userId).stream()
                .map(SysRole::getCode)
                .toList();

        String communityName = null;
        if (user.getCommunityId() != null) {
            SysCommunity community = communityMapper.selectById(user.getCommunityId());
            communityName = community == null ? null : community.getName();
        }

        long complaintTotal = complaintMapper.selectCount(Wrappers.<SysComplaint>lambdaQuery()
                .eq(SysComplaint::getSubmitterId, userId));
        long complaintPending = complaintMapper.selectCount(Wrappers.<SysComplaint>lambdaQuery()
                .eq(SysComplaint::getSubmitterId, userId)
                .eq(SysComplaint::getStatus, 0));
        long complaintProcessing = complaintMapper.selectCount(Wrappers.<SysComplaint>lambdaQuery()
                .eq(SysComplaint::getSubmitterId, userId)
                .eq(SysComplaint::getStatus, 1));
        long complaintResolved = complaintMapper.selectCount(Wrappers.<SysComplaint>lambdaQuery()
                .eq(SysComplaint::getSubmitterId, userId)
                .eq(SysComplaint::getStatus, 2));
        long satisfactionTotal = satisfactionMapper.selectCount(Wrappers.<SysSatisfaction>lambdaQuery()
                .eq(SysSatisfaction::getUserId, userId));

        String userName = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();

        return new AppProfileVO(user.getId(), userName, user.getUsername(),
                user.getMobile() == null ? "" : user.getMobile(),
                user.getEmail() == null ? "" : user.getEmail(),
                roles, user.getCommunityId(), communityName, user.getCreatedAt(),
                complaintTotal, complaintPending, complaintProcessing, complaintResolved,
                satisfactionTotal);
    }

    @Override
    public void updateProfile(Long userId, AppProfileUpdateRequest request) {
        SysUser user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        SysUser update = new SysUser();
        update.setId(userId);

        if (StringUtils.hasText(request.nickname())) {
            update.setNickname(request.nickname().trim());
        }

        if (request.mobile() != null) {
            update.setMobile(request.mobile().trim());
        }

        if (request.email() != null) {
            update.setEmail(request.email().trim());
        }

        userMapper.updateById(update);
    }

    @Override
    @Transactional
    public void bindCommunity(Long userId, Long communityId) {
        SysUser user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        SysCommunity community = communityMapper.selectById(communityId);

        if (community == null || !Integer.valueOf(1).equals(community.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "小区不存在或已停用");
        }

        SysUser update = new SysUser();
        update.setId(userId);
        update.setCommunityId(communityId);
        userMapper.updateById(update);

        SysUserCommunityLog log = new SysUserCommunityLog();
        log.setUserId(userId);
        log.setCommunityId(communityId);
        log.setOperatorId(userId);
        log.setOperatorName(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
        bindingLogMapper.insert(log);
    }
}

package com.renewcommunity.renewcommunity.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysCommunity;
import com.renewcommunity.renewcommunity.entity.SysRole;
import com.renewcommunity.renewcommunity.entity.SysUser;
import com.renewcommunity.renewcommunity.entity.SysUserCommunityLog;
import com.renewcommunity.renewcommunity.entity.SysUserRole;
import com.renewcommunity.renewcommunity.mapper.SysCommunityMapper;
import com.renewcommunity.renewcommunity.mapper.SysRoleMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserCommunityLogMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserRoleMapper;
import com.renewcommunity.renewcommunity.model.vo.CommunityBindingVO;
import com.renewcommunity.renewcommunity.model.vo.ResidentVO;
import com.renewcommunity.renewcommunity.service.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 居民用户管理实现。
 */
@Service
@RequiredArgsConstructor
public class ResidentServiceImpl implements ResidentService {

    private static final String RESIDENT_ROLE_CODE = "R_USER";

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    private final SysUserRoleMapper userRoleMapper;

    private final SysCommunityMapper communityMapper;

    private final SysUserCommunityLogMapper bindingLogMapper;

    @Override
    public PageResult<ResidentVO> listResidents(String keyword, Long communityId, int pageNum, int pageSize) {
        List<Long> userIds = residentUserIds();

        if (userIds.isEmpty()) {
            return new PageResult<>(0, List.of());
        }

        Page<SysUser> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 200));
        var query = Wrappers.<SysUser>lambdaQuery().in(SysUser::getId, userIds);

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getNickname, keyword)
                    .or()
                    .like(SysUser::getMobile, keyword));
        }

        if (communityId != null) {
            query.eq(SysUser::getCommunityId, communityId);
        }

        query.orderByDesc(SysUser::getId);
        Page<SysUser> result = userMapper.selectPage(page, query);
        Map<Long, String> communityNames = communityNameMap(result.getRecords().stream()
                .map(SysUser::getCommunityId)
                .filter(Objects::nonNull)
                .toList());

        List<ResidentVO> list = result.getRecords().stream()
                .map(user -> new ResidentVO(user.getId(), user.getUsername(), user.getNickname(), user.getMobile(),
                        user.getEmail(), user.getStatus(), user.getCommunityId(),
                        communityNames.get(user.getCommunityId()), user.getCreatedAt()))
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    @Transactional
    public void bindCommunity(Long userId, Long communityId) {
        if (!residentUserIds().contains(userId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该用户不是居民账号");
        }

        SysCommunity community = communityMapper.selectById(communityId);
        if (community == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "小区不存在");
        }

        SysUser update = new SysUser();
        update.setId(userId);
        update.setCommunityId(communityId);
        userMapper.updateById(update);

        Long operatorId = StpUtil.getLoginIdAsLong();
        SysUserCommunityLog log = new SysUserCommunityLog();
        log.setUserId(userId);
        log.setCommunityId(communityId);
        log.setOperatorId(operatorId);
        log.setOperatorName(resolveUserName(operatorId));
        bindingLogMapper.insert(log);
    }

    @Override
    public List<CommunityBindingVO> listBindings(Long userId) {
        List<SysUserCommunityLog> logs = bindingLogMapper.selectList(Wrappers.<SysUserCommunityLog>lambdaQuery()
                .eq(SysUserCommunityLog::getUserId, userId)
                .orderByDesc(SysUserCommunityLog::getId));
        Map<Long, String> communityNames = communityNameMap(logs.stream()
                .map(SysUserCommunityLog::getCommunityId)
                .filter(Objects::nonNull)
                .toList());

        return logs.stream()
                .map(log -> new CommunityBindingVO(log.getId(), log.getCommunityId(),
                        communityNames.get(log.getCommunityId()), log.getOperatorName(), log.getCreatedAt()))
                .toList();
    }

    private List<Long> residentUserIds() {
        SysRole role = roleMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getCode, RESIDENT_ROLE_CODE));
        if (role == null) {
            return List.of();
        }

        return userRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                        .eq(SysUserRole::getRoleId, role.getId()))
                .stream()
                .map(SysUserRole::getUserId)
                .distinct()
                .toList();
    }

    private Map<Long, String> communityNameMap(Collection<Long> communityIds) {
        if (communityIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return communityMapper.selectBatchIds(communityIds).stream()
                .collect(Collectors.toMap(SysCommunity::getId, SysCommunity::getName));
    }

    private String resolveUserName(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return "用户" + userId;
        }

        return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
    }
}

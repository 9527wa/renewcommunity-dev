package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysCommunity;
import com.renewcommunity.renewcommunity.entity.SysProject;
import com.renewcommunity.renewcommunity.entity.SysUser;
import com.renewcommunity.renewcommunity.mapper.SysCommunityMapper;
import com.renewcommunity.renewcommunity.mapper.SysProjectMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserMapper;
import com.renewcommunity.renewcommunity.model.dto.CommunitySaveRequest;
import com.renewcommunity.renewcommunity.model.vo.CommunityVO;
import com.renewcommunity.renewcommunity.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 小区管理实现。
 */
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final SysCommunityMapper communityMapper;

    private final SysProjectMapper projectMapper;

    private final SysUserMapper userMapper;

    @Override
    public PageResult<CommunityVO> listCommunities(String keyword, Integer status, int pageNum, int pageSize) {
        Page<SysCommunity> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 200));
        var query = Wrappers.<SysCommunity>lambdaQuery();

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like(SysCommunity::getName, keyword)
                    .or()
                    .like(SysCommunity::getAddress, keyword)
                    .or()
                    .like(SysCommunity::getManager, keyword));
        }

        if (status != null) {
            query.eq(SysCommunity::getStatus, status);
        }

        query.orderByAsc(SysCommunity::getId);
        Page<SysCommunity> result = communityMapper.selectPage(page, query);
        List<CommunityVO> list = result.getRecords().stream().map(CommunityServiceImpl::toVO).toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    public List<CommunityVO> listAllCommunities() {
        return communityMapper.selectList(Wrappers.<SysCommunity>lambdaQuery()
                        .eq(SysCommunity::getStatus, 1)
                        .orderByAsc(SysCommunity::getId))
                .stream()
                .map(CommunityServiceImpl::toVO)
                .toList();
    }

    @Override
    public Long createCommunity(CommunitySaveRequest request) {
        long count = communityMapper.selectCount(Wrappers.<SysCommunity>lambdaQuery()
                .eq(SysCommunity::getName, request.name()));
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "小区名称已存在");
        }

        SysCommunity community = new SysCommunity();
        apply(community, request);
        communityMapper.insert(community);

        return community.getId();
    }

    @Override
    public void updateCommunity(Long id, CommunitySaveRequest request) {
        requireCommunity(id);

        long count = communityMapper.selectCount(Wrappers.<SysCommunity>lambdaQuery()
                .eq(SysCommunity::getName, request.name())
                .ne(SysCommunity::getId, id));
        if (count > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "小区名称已存在");
        }

        SysCommunity community = new SysCommunity();
        community.setId(id);
        apply(community, request);
        communityMapper.updateById(community);
    }

    @Override
    public void deleteCommunity(Long id) {
        SysCommunity community = requireCommunity(id);

        long projectCount = projectMapper.selectCount(Wrappers.<SysProject>lambdaQuery()
                .eq(SysProject::getCommunity, community.getName()));
        if (projectCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该小区下存在改造项目，无法删除");
        }

        long residentCount = userMapper.selectCount(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getCommunityId, id));
        if (residentCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该小区已绑定居民用户，无法删除");
        }

        communityMapper.deleteById(id);
    }

    private void apply(SysCommunity community, CommunitySaveRequest request) {
        community.setName(request.name());
        community.setAddress(defaultString(request.address()));
        community.setBuiltYear(request.builtYear());
        community.setBuildingCount(request.buildingCount() == null ? 0 : request.buildingCount());
        community.setHouseholdCount(request.householdCount() == null ? 0 : request.householdCount());
        community.setManager(defaultString(request.manager()));
        community.setManagerMobile(defaultString(request.managerMobile()));
        community.setStatus(request.status() == null ? 1 : request.status());
        community.setRemark(defaultString(request.remark()));
    }

    private SysCommunity requireCommunity(Long id) {
        SysCommunity community = id == null ? null : communityMapper.selectById(id);
        if (community == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "小区不存在");
        }

        return community;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private static CommunityVO toVO(SysCommunity community) {
        return new CommunityVO(community.getId(), community.getName(), community.getAddress(),
                community.getBuiltYear(), community.getBuildingCount(), community.getHouseholdCount(),
                community.getManager(), community.getManagerMobile(), community.getStatus(), community.getRemark());
    }
}

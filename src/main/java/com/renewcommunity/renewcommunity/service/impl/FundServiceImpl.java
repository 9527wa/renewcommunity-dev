package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysFund;
import com.renewcommunity.renewcommunity.entity.SysProject;
import com.renewcommunity.renewcommunity.mapper.SysFundMapper;
import com.renewcommunity.renewcommunity.mapper.SysProjectMapper;
import com.renewcommunity.renewcommunity.model.dto.FundSaveRequest;
import com.renewcommunity.renewcommunity.model.vo.FundVO;
import com.renewcommunity.renewcommunity.service.FundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 资金台账实现。
 */
@Service
@RequiredArgsConstructor
public class FundServiceImpl implements FundService {

    private final SysFundMapper fundMapper;

    private final SysProjectMapper projectMapper;

    @Override
    public PageResult<FundVO> listFunds(String keyword, Integer type, Long projectId, int pageNum, int pageSize) {
        Page<SysFund> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 200));
        var query = Wrappers.<SysFund>lambdaQuery();

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like(SysFund::getName, keyword)
                    .or()
                    .like(SysFund::getRemark, keyword));
        }

        if (type != null) {
            query.eq(SysFund::getType, type);
        }

        if (projectId != null) {
            query.eq(SysFund::getProjectId, projectId);
        }

        query.orderByDesc(SysFund::getOccurredAt).orderByDesc(SysFund::getId);
        Page<SysFund> result = fundMapper.selectPage(page, query);
        Map<Long, String> projectNames = projectNameMap(result.getRecords().stream()
                .map(SysFund::getProjectId)
                .filter(Objects::nonNull)
                .toList());

        List<FundVO> list = result.getRecords().stream()
                .map(item -> toVO(item, projectNames.get(item.getProjectId())))
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    public Long createFund(FundSaveRequest request) {
        validateProject(request.projectId());

        SysFund fund = new SysFund();
        apply(fund, request);
        fundMapper.insert(fund);

        return fund.getId();
    }

    @Override
    public void updateFund(Long id, FundSaveRequest request) {
        requireFund(id);
        validateProject(request.projectId());

        SysFund fund = new SysFund();
        fund.setId(id);
        apply(fund, request);
        fundMapper.updateById(fund);
    }

    @Override
    public void deleteFund(Long id) {
        requireFund(id);
        fundMapper.deleteById(id);
    }

    private void apply(SysFund fund, FundSaveRequest request) {
        fund.setProjectId(request.projectId());
        fund.setName(request.name());
        fund.setType(request.type());
        fund.setAmount(request.amount());
        fund.setOccurredAt(request.occurredAt());
        fund.setRemark(request.remark() == null ? "" : request.remark());
    }

    private void validateProject(Long projectId) {
        if (projectId != null && projectMapper.selectById(projectId) == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "关联项目不存在");
        }
    }

    private void requireFund(Long id) {
        if (id == null || fundMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资金记录不存在");
        }
    }

    private Map<Long, String> projectNameMap(Collection<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Map.of();
        }

        return projectMapper.selectBatchIds(projectIds).stream()
                .collect(Collectors.toMap(SysProject::getId, SysProject::getName));
    }

    private static FundVO toVO(SysFund fund, String projectName) {
        return new FundVO(fund.getId(), fund.getProjectId(), projectName, fund.getName(), fund.getType(),
                fund.getAmount(), fund.getOccurredAt(), fund.getRemark());
    }
}

package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysComplaint;
import com.renewcommunity.renewcommunity.entity.SysSatisfaction;
import com.renewcommunity.renewcommunity.entity.SysUser;
import com.renewcommunity.renewcommunity.mapper.SysComplaintMapper;
import com.renewcommunity.renewcommunity.mapper.SysSatisfactionMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserMapper;
import com.renewcommunity.renewcommunity.model.dto.AppSatisfactionSubmitRequest;
import com.renewcommunity.renewcommunity.model.vo.SatisfactionVO;
import com.renewcommunity.renewcommunity.service.AppSatisfactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * C 端满意度评价实现。
 */
@Service
@RequiredArgsConstructor
public class AppSatisfactionServiceImpl implements AppSatisfactionService {

    private final SysSatisfactionMapper satisfactionMapper;

    private final SysComplaintMapper complaintMapper;

    private final SysUserMapper userMapper;

    @Override
    public Long submit(Long userId, AppSatisfactionSubmitRequest request) {
        SysComplaint complaint = complaintMapper.selectById(request.complaintId());

        if (complaint == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "投诉不存在");
        }

        if (!userId.equals(complaint.getSubmitterId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能评价自己提交的投诉");
        }

        if (!Integer.valueOf(2).equals(complaint.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "投诉办结后才能评价");
        }

        long exists = satisfactionMapper.selectCount(Wrappers.<SysSatisfaction>lambdaQuery()
                .eq(SysSatisfaction::getComplaintId, request.complaintId()));
        if (exists > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "该投诉已评价");
        }

        SysUser user = userMapper.selectById(userId);
        String userName = user == null
                ? "居民" + userId
                : (StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());

        SysSatisfaction satisfaction = new SysSatisfaction();
        satisfaction.setComplaintId(request.complaintId());
        satisfaction.setUserId(userId);
        satisfaction.setUserName(userName);
        satisfaction.setScore(request.score());
        satisfaction.setComment(request.comment() == null ? "" : request.comment());
        satisfactionMapper.insert(satisfaction);

        return satisfaction.getId();
    }

    @Override
    public PageResult<SatisfactionVO> listMy(Long userId, int pageNum, int pageSize) {
        Page<SysSatisfaction> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 100));
        var query = Wrappers.<SysSatisfaction>lambdaQuery()
                .eq(SysSatisfaction::getUserId, userId)
                .orderByDesc(SysSatisfaction::getCreatedAt)
                .orderByDesc(SysSatisfaction::getId);
        Page<SysSatisfaction> result = satisfactionMapper.selectPage(page, query);
        Map<Long, String> complaintTitles = complaintTitleMap(result.getRecords().stream()
                .map(SysSatisfaction::getComplaintId)
                .toList());

        List<SatisfactionVO> list = result.getRecords().stream()
                .map(item -> new SatisfactionVO(item.getId(), item.getComplaintId(),
                        complaintTitles.get(item.getComplaintId()), item.getUserName(), item.getScore(),
                        item.getComment(), item.getCreatedAt()))
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    private Map<Long, String> complaintTitleMap(Collection<Long> complaintIds) {
        if (complaintIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return complaintMapper.selectBatchIds(complaintIds).stream()
                .collect(Collectors.toMap(SysComplaint::getId, SysComplaint::getTitle));
    }
}

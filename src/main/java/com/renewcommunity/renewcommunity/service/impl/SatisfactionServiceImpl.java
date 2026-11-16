package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.entity.SysComplaint;
import com.renewcommunity.renewcommunity.entity.SysSatisfaction;
import com.renewcommunity.renewcommunity.mapper.SysComplaintMapper;
import com.renewcommunity.renewcommunity.mapper.SysSatisfactionMapper;
import com.renewcommunity.renewcommunity.model.vo.SatisfactionStatsVO;
import com.renewcommunity.renewcommunity.model.vo.SatisfactionVO;
import com.renewcommunity.renewcommunity.service.SatisfactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 满意度管理实现。
 */
@Service
@RequiredArgsConstructor
public class SatisfactionServiceImpl implements SatisfactionService {

    private final SysSatisfactionMapper satisfactionMapper;

    private final SysComplaintMapper complaintMapper;

    @Override
    public PageResult<SatisfactionVO> listSatisfactions(Integer score, int pageNum, int pageSize) {
        Page<SysSatisfaction> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 200));
        var query = Wrappers.<SysSatisfaction>lambdaQuery();

        if (score != null) {
            query.eq(SysSatisfaction::getScore, score);
        }

        query.orderByDesc(SysSatisfaction::getCreatedAt).orderByDesc(SysSatisfaction::getId);
        Page<SysSatisfaction> result = satisfactionMapper.selectPage(page, query);
        Map<Long, String> complaintTitles = complaintTitleMap(result.getRecords().stream()
                .map(SysSatisfaction::getComplaintId)
                .filter(Objects::nonNull)
                .toList());

        List<SatisfactionVO> list = result.getRecords().stream()
                .map(item -> new SatisfactionVO(item.getId(), item.getComplaintId(),
                        complaintTitles.get(item.getComplaintId()), item.getUserName(), item.getScore(),
                        item.getComment(), item.getCreatedAt()))
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    public SatisfactionStatsVO getStats() {
        long total = satisfactionMapper.selectCount(null);
        long five = satisfactionMapper.countByScore(5);
        long four = satisfactionMapper.countByScore(4);
        long three = satisfactionMapper.countByScore(3);
        long two = satisfactionMapper.countByScore(2);
        long one = satisfactionMapper.countByScore(1);
        double average = satisfactionMapper.averageScore();
        long good = satisfactionMapper.countAtLeast(4);
        double goodRate = total > 0 ? round(good * 100.0 / total) : 0;

        return new SatisfactionStatsVO(total, average, five, four, goodRate, one, two, three);
    }

    private Map<Long, String> complaintTitleMap(Collection<Long> complaintIds) {
        if (complaintIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return complaintMapper.selectBatchIds(complaintIds).stream()
                .collect(Collectors.toMap(SysComplaint::getId, SysComplaint::getTitle));
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}

package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.renewcommunity.renewcommunity.entity.SysComplaint;
import com.renewcommunity.renewcommunity.entity.SysDocument;
import com.renewcommunity.renewcommunity.entity.SysProgress;
import com.renewcommunity.renewcommunity.entity.SysProject;
import com.renewcommunity.renewcommunity.mapper.SysComplaintMapper;
import com.renewcommunity.renewcommunity.mapper.SysDocumentMapper;
import com.renewcommunity.renewcommunity.mapper.SysFundMapper;
import com.renewcommunity.renewcommunity.mapper.SysProgressMapper;
import com.renewcommunity.renewcommunity.mapper.SysProjectMapper;
import com.renewcommunity.renewcommunity.mapper.SysSatisfactionMapper;
import com.renewcommunity.renewcommunity.model.vo.StatisticsVO;
import com.renewcommunity.renewcommunity.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统计分析实现。
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SysProjectMapper projectMapper;

    private final SysComplaintMapper complaintMapper;

    private final SysFundMapper fundMapper;

    private final SysDocumentMapper documentMapper;

    private final SysSatisfactionMapper satisfactionMapper;

    private final SysProgressMapper progressMapper;

    @Override
    public StatisticsVO getOverview() {
        long projectTotal = projectMapper.selectCount(null);
        long planning = projectMapper.countByStatus(0);
        long ongoing = projectMapper.countByStatus(1);
        long completed = projectMapper.countByStatus(2);
        int averageProgress = projectMapper.averageProgress();
        List<StatisticsVO.NameCount> projectCategories = groupCategories(
                projectMapper.selectList(null).stream().map(SysProject::getCategory).toList(), "未分类");

        long complaintTotal = complaintMapper.selectCount(null);
        long pending = complaintMapper.countByStatus(0);
        long processing = complaintMapper.countByStatus(1);
        long resolved = complaintMapper.countByStatus(2);
        long thisMonth = complaintMapper.countCreatedAfter(LocalDate.now().withDayOfMonth(1).atStartOfDay());
        List<StatisticsVO.NameCount> complaintTypes = groupCategories(
                complaintMapper.selectList(null).stream().map(SysComplaint::getType).toList(), "其他");

        BigDecimal budget = projectMapper.sumBudget();
        BigDecimal allocated = fundMapper.sumByType(1);
        BigDecimal spent = fundMapper.sumByType(2);
        double executionRate = budget.compareTo(BigDecimal.ZERO) > 0
                ? round(spent.doubleValue() * 100 / budget.doubleValue())
                : 0;

        long documentTotal = documentMapper.selectCount(null);
        long archived = documentMapper.selectCount(Wrappers.<SysDocument>lambdaQuery()
                .eq(SysDocument::getStatus, 1));
        List<StatisticsVO.NameCount> documentCategories = groupCategories(
                documentMapper.selectList(null).stream().map(SysDocument::getCategory).toList(), "其他");

        long satisfactionTotal = satisfactionMapper.selectCount(null);
        double averageScore = satisfactionMapper.averageScore();
        long good = satisfactionMapper.countAtLeast(4);
        double goodRate = satisfactionTotal > 0 ? round(good * 100.0 / satisfactionTotal) : 0;

        long progressTotal = progressMapper.selectCount(null);
        long progressThisMonth = progressMapper.selectCount(Wrappers.<SysProgress>lambdaQuery()
                .ge(SysProgress::getReportDate, LocalDate.now().withDayOfMonth(1)));

        return new StatisticsVO(
                new StatisticsVO.ProjectStats(projectTotal, planning, ongoing, completed, averageProgress,
                        projectCategories),
                new StatisticsVO.ComplaintStats(complaintTotal, pending, processing, resolved, thisMonth,
                        complaintTypes),
                new StatisticsVO.FundStats(budget, allocated, spent, executionRate),
                new StatisticsVO.DocumentStats(documentTotal, archived, documentCategories),
                new StatisticsVO.SatisfactionStats(satisfactionTotal, averageScore, goodRate),
                new StatisticsVO.ProgressStats(progressTotal, progressThisMonth)
        );
    }

    private List<StatisticsVO.NameCount> groupCategories(List<String> categories, String defaultName) {
        Map<String, Long> counts = categories.stream()
                .map(category -> StringUtils.hasText(category) ? category : defaultName)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return counts.entrySet().stream()
                .map(entry -> new StatisticsVO.NameCount(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(StatisticsVO.NameCount::count).reversed())
                .toList();
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}

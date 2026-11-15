package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.renewcommunity.renewcommunity.entity.SysComplaint;
import com.renewcommunity.renewcommunity.entity.SysProject;
import com.renewcommunity.renewcommunity.mapper.SysComplaintMapper;
import com.renewcommunity.renewcommunity.mapper.SysFundMapper;
import com.renewcommunity.renewcommunity.mapper.SysProjectMapper;
import com.renewcommunity.renewcommunity.model.vo.DashboardVO;
import com.renewcommunity.renewcommunity.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 首页工作台统计实现。
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_LIMIT = 5;

    private final SysProjectMapper projectMapper;

    private final SysComplaintMapper complaintMapper;

    private final SysFundMapper fundMapper;

    @Override
    public DashboardVO getDashboard() {
        long projectTotal = projectMapper.selectCount(null);
        long planning = projectMapper.countByStatus(0);
        long ongoing = projectMapper.countByStatus(1);
        long completed = projectMapper.countByStatus(2);
        int averageProgress = projectMapper.averageProgress();

        long complaintTotal = complaintMapper.selectCount(null);
        long pending = complaintMapper.countByStatus(0);
        long processing = complaintMapper.countByStatus(1);
        long resolved = complaintMapper.countByStatus(2);
        long thisMonth = complaintMapper.countCreatedAfter(LocalDate.now().withDayOfMonth(1).atStartOfDay());

        BigDecimal budget = projectMapper.sumBudget();
        BigDecimal allocated = fundMapper.sumByType(1);
        BigDecimal spent = fundMapper.sumByType(2);
        double executionRate = budget.compareTo(BigDecimal.ZERO) > 0
                ? round(spent.doubleValue() * 100 / budget.doubleValue())
                : 0;

        var recentProjects = projectMapper.selectList(Wrappers.<SysProject>lambdaQuery()
                        .orderByDesc(SysProject::getId)
                        .last("LIMIT " + RECENT_LIMIT))
                .stream()
                .map(item -> new DashboardVO.ProjectBrief(item.getId(), item.getName(), item.getCommunity(),
                        item.getStatus(), item.getProgress(), item.getBudget()))
                .toList();

        var recentComplaints = complaintMapper.selectList(Wrappers.<SysComplaint>lambdaQuery()
                        .orderByDesc(SysComplaint::getCreatedAt)
                        .orderByDesc(SysComplaint::getId)
                        .last("LIMIT " + RECENT_LIMIT))
                .stream()
                .map(item -> new DashboardVO.ComplaintBrief(item.getId(), item.getTitle(), item.getCommunity(),
                        item.getStatus(), item.getCreatedAt()))
                .toList();

        return new DashboardVO(
                new DashboardVO.ProjectStats(projectTotal, planning, ongoing, completed, averageProgress),
                new DashboardVO.ComplaintStats(complaintTotal, pending, processing, resolved, thisMonth),
                new DashboardVO.FundStats(budget, allocated, spent, executionRate),
                recentProjects,
                recentComplaints
        );
    }

    private double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
}

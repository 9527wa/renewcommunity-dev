package com.renewcommunity.renewcommunity.model.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端首页工作台统计。
 */
public record DashboardVO(
        ProjectStats project,
        ComplaintStats complaint,
        FundStats fund,
        List<ProjectBrief> recentProjects,
        List<ComplaintBrief> recentComplaints
) {

    /**
     * 项目统计。
     */
    public record ProjectStats(long total,
                               long planning,
                               long ongoing,
                               long completed,
                               int averageProgress) {
    }

    /**
     * 投诉统计。
     */
    public record ComplaintStats(long total,
                                 long pending,
                                 long processing,
                                 long resolved,
                                 long thisMonth) {
    }

    /**
     * 资金统计，金额单位为元。
     */
    public record FundStats(BigDecimal budget,
                            BigDecimal allocated,
                            BigDecimal spent,
                            double executionRate) {
    }

    /**
     * 最新项目摘要。
     */
    public record ProjectBrief(Long id,
                               String name,
                               String community,
                               Integer status,
                               Integer progress,
                               BigDecimal budget) {
    }

    /**
     * 最新投诉摘要。
     */
    public record ComplaintBrief(Long id,
                                 String title,
                                 String community,
                                 Integer status,
                                 LocalDateTime createdAt) {
    }
}

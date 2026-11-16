package com.renewcommunity.renewcommunity.model.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 统计分析总览。
 */
public record StatisticsVO(
        ProjectStats project,
        ComplaintStats complaint,
        FundStats fund,
        DocumentStats document,
        SatisfactionStats satisfaction,
        ProgressStats progress
) {

    public record ProjectStats(long total, long planning, long ongoing, long completed,
                               int averageProgress, List<NameCount> categories) {
    }

    public record ComplaintStats(long total, long pending, long processing, long resolved,
                                 long thisMonth, List<NameCount> types) {
    }

    public record FundStats(BigDecimal budget, BigDecimal allocated, BigDecimal spent, double executionRate) {
    }

    public record DocumentStats(long total, long archived, List<NameCount> categories) {
    }

    public record SatisfactionStats(long total, double averageScore, double goodRate) {
    }

    public record ProgressStats(long total, long thisMonth) {
    }

    public record NameCount(String name, long count) {
    }
}

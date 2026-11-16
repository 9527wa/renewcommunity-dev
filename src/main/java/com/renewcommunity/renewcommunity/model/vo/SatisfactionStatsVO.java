package com.renewcommunity.renewcommunity.model.vo;

/**
 * 满意度评分统计。
 */
public record SatisfactionStatsVO(
        long total,
        double averageScore,
        long fiveCount,
        long fourCount,
        double goodRate,
        long oneCount,
        long twoCount,
        long threeCount
) {
}

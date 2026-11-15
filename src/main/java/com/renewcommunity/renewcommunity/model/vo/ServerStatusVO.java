package com.renewcommunity.renewcommunity.model.vo;

/**
 * 服务器运行状态与内存状态。
 *
 * <p>内存字段单位均为字节；无法获取的指标以 {@code -1} 表示。</p>
 *
 * @param os     操作系统与 CPU 状态
 * @param memory 物理内存与 JVM 内存状态
 * @param jvm    JVM 运行状态
 * @param app    应用信息
 */
public record ServerStatusVO(OsInfo os, MemoryInfo memory, JvmInfo jvm, AppInfo app) {

    /**
     * 操作系统与 CPU 状态。
     *
     * @param name               操作系统名称
     * @param arch               系统架构
     * @param version            系统版本
     * @param availableProcessors 可用处理器数
     * @param systemLoadAverage  系统负载（-1 表示不可用）
     * @param cpuUsage           CPU 使用率百分比（-1 表示不可用）
     */
    public record OsInfo(String name,
                         String arch,
                         String version,
                         int availableProcessors,
                         double systemLoadAverage,
                         double cpuUsage) {
    }

    /**
     * 内存状态。
     *
     * @param physicalTotal   物理内存总量
     * @param physicalUsed    已用物理内存
     * @param physicalFree    空闲物理内存
     * @param physicalUsage   物理内存使用率百分比
     * @param heapUsed        JVM 堆已用内存
     * @param heapMax         JVM 堆最大内存
     * @param heapCommitted   JVM 堆已提交内存
     * @param heapUsage       JVM 堆使用率百分比
     * @param nonHeapUsed     JVM 非堆已用内存
     * @param nonHeapCommitted JVM 非堆已提交内存
     */
    public record MemoryInfo(long physicalTotal,
                             long physicalUsed,
                             long physicalFree,
                             double physicalUsage,
                             long heapUsed,
                             long heapMax,
                             long heapCommitted,
                             double heapUsage,
                             long nonHeapUsed,
                             long nonHeapCommitted) {
    }

    /**
     * JVM 运行状态。
     *
     * @param javaVersion      Java 版本
     * @param javaVendor       Java 供应商
     * @param jvmName          JVM 名称
     * @param startTime        启动时间戳（毫秒）
     * @param uptimeSeconds    已运行秒数
     * @param threadCount      当前线程数
     * @param peakThreadCount  峰值线程数
     * @param daemonThreadCount 守护线程数
     * @param loadedClassCount 已加载类数量
     */
    public record JvmInfo(String javaVersion,
                          String javaVendor,
                          String jvmName,
                          long startTime,
                          long uptimeSeconds,
                          int threadCount,
                          int peakThreadCount,
                          int daemonThreadCount,
                          long loadedClassCount) {
    }

    /**
     * 应用信息。
     *
     * @param name           应用名称
     * @param activeProfiles 激活的 Spring Profile，多个以逗号分隔
     */
    public record AppInfo(String name, String activeProfiles) {
    }
}

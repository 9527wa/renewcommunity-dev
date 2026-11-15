package com.renewcommunity.renewcommunity.service.impl;

import com.renewcommunity.renewcommunity.model.vo.ServerStatusVO;
import com.renewcommunity.renewcommunity.service.SystemMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;

/**
 * 服务器监控实现，数据来源于 JDK 的 MXBean，不依赖额外组件。
 */
@Service
@RequiredArgsConstructor
public class SystemMonitorServiceImpl implements SystemMonitorService {

    private static final String DEFAULT_APP_NAME = "RenewCommunity";

    private final Environment environment;

    @Override
    public ServerStatusVO getServerStatus() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        long heapMax = heap.getMax() > 0 ? heap.getMax() : Runtime.getRuntime().maxMemory();
        double heapUsage = heapMax > 0 ? percent(heap.getUsed(), heapMax) : -1;

        long physicalTotal = -1;
        long physicalFree = -1;
        double cpuUsage = -1;

        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
            physicalTotal = sunOsBean.getTotalMemorySize();
            physicalFree = sunOsBean.getFreeMemorySize();
            double cpuLoad = sunOsBean.getCpuLoad();
            if (cpuLoad >= 0) {
                cpuUsage = round(cpuLoad * 100);
            }
        }

        long physicalUsed = physicalTotal > 0 && physicalFree >= 0 ? physicalTotal - physicalFree : -1;
        double physicalUsage = physicalTotal > 0 && physicalUsed > 0 ? percent(physicalUsed, physicalTotal) : -1;

        ServerStatusVO.OsInfo os = new ServerStatusVO.OsInfo(
                osBean.getName(),
                osBean.getArch(),
                osBean.getVersion(),
                osBean.getAvailableProcessors(),
                round(osBean.getSystemLoadAverage()),
                cpuUsage
        );

        ServerStatusVO.MemoryInfo memory = new ServerStatusVO.MemoryInfo(
                physicalTotal,
                physicalUsed,
                physicalFree,
                physicalUsage,
                heap.getUsed(),
                heapMax,
                heap.getCommitted(),
                heapUsage,
                nonHeap.getUsed(),
                nonHeap.getCommitted()
        );

        ServerStatusVO.JvmInfo jvm = new ServerStatusVO.JvmInfo(
                System.getProperty("java.version"),
                System.getProperty("java.vendor"),
                System.getProperty("java.vm.name"),
                runtimeBean.getStartTime(),
                runtimeBean.getUptime() / 1000,
                threadBean.getThreadCount(),
                threadBean.getPeakThreadCount(),
                threadBean.getDaemonThreadCount(),
                classLoadingBean.getLoadedClassCount()
        );

        ServerStatusVO.AppInfo app = new ServerStatusVO.AppInfo(
                environment.getProperty("spring.application.name", DEFAULT_APP_NAME),
                String.join(",", environment.getActiveProfiles())
        );

        return new ServerStatusVO(os, memory, jvm, app);
    }

    private double percent(long used, long total) {
        return round((double) used * 100 / total);
    }

    private double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
}

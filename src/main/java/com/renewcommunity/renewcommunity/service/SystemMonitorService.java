package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.model.vo.ServerStatusVO;

/**
 * 服务器监控服务。
 */
public interface SystemMonitorService {

    /**
     * 获取服务器运行状态与内存状态。
     */
    ServerStatusVO getServerStatus();
}

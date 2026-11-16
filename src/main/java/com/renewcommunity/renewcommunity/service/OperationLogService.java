package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.vo.OperationLogVO;

/**
 * 操作日志服务。
 */
public interface OperationLogService {

    void record(Long userId, String method, String path, String ip, long durationMs, boolean success, String errorMsg);

    PageResult<OperationLogVO> listLogs(String keyword, Integer success, int pageNum, int pageSize);
}

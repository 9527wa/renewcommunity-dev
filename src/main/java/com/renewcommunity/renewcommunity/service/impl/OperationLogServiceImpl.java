package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.entity.SysOperationLog;
import com.renewcommunity.renewcommunity.entity.SysUser;
import com.renewcommunity.renewcommunity.mapper.SysOperationLogMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserMapper;
import com.renewcommunity.renewcommunity.model.vo.OperationLogVO;
import com.renewcommunity.renewcommunity.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 操作日志实现。
 */
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private static final int MAX_ERROR_LENGTH = 500;

    private final SysOperationLogMapper operationLogMapper;

    private final SysUserMapper userMapper;

    @Override
    public void record(Long userId, String method, String path, String ip, long durationMs, boolean success,
                       String errorMsg) {
        SysOperationLog log = new SysOperationLog();
        log.setUserId(userId);
        log.setUserName(resolveUserName(userId));
        log.setMethod(method);
        log.setPath(path);
        log.setIp(ip);
        log.setDurationMs(durationMs);
        log.setSuccess(success ? 1 : 0);
        String message = errorMsg == null ? "" : errorMsg;
        log.setErrorMsg(message.length() > MAX_ERROR_LENGTH ? message.substring(0, MAX_ERROR_LENGTH) : message);
        operationLogMapper.insert(log);
    }

    @Override
    public PageResult<OperationLogVO> listLogs(String keyword, Integer success, int pageNum, int pageSize) {
        Page<SysOperationLog> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 200));
        var query = Wrappers.<SysOperationLog>lambdaQuery();

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like(SysOperationLog::getUserName, keyword)
                    .or()
                    .like(SysOperationLog::getPath, keyword)
                    .or()
                    .like(SysOperationLog::getIp, keyword));
        }

        if (success != null) {
            query.eq(SysOperationLog::getSuccess, success);
        }

        query.orderByDesc(SysOperationLog::getId);
        Page<SysOperationLog> result = operationLogMapper.selectPage(page, query);

        List<OperationLogVO> list = result.getRecords().stream()
                .map(log -> new OperationLogVO(log.getId(), log.getUserId(), log.getUserName(), log.getMethod(),
                        log.getPath(), log.getIp(), log.getDurationMs(), log.getSuccess(), log.getErrorMsg(),
                        log.getCreatedAt()))
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    private String resolveUserName(Long userId) {
        if (userId == null) {
            return "匿名";
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return "用户" + userId;
        }

        return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
    }
}

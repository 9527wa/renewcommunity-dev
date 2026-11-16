package com.renewcommunity.renewcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.renewcommunity.renewcommunity.entity.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志数据访问。
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {
}

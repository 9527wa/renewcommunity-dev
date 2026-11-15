package com.renewcommunity.renewcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.renewcommunity.renewcommunity.entity.SysComplaint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 投诉数据访问。
 */
@Mapper
public interface SysComplaintMapper extends BaseMapper<SysComplaint> {

    @Select("SELECT COUNT(*) FROM sys_complaint WHERE status = #{status}")
    long countByStatus(@Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM sys_complaint WHERE created_at >= #{start}")
    long countCreatedAfter(@Param("start") LocalDateTime start);
}

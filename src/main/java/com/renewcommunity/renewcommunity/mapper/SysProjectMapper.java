package com.renewcommunity.renewcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.renewcommunity.renewcommunity.entity.SysProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 项目数据访问。
 */
@Mapper
public interface SysProjectMapper extends BaseMapper<SysProject> {

    @Select("SELECT COUNT(*) FROM sys_project WHERE status = #{status}")
    long countByStatus(@Param("status") Integer status);

    @Select("SELECT COALESCE(ROUND(AVG(progress)), 0) FROM sys_project")
    int averageProgress();

    @Select("SELECT COALESCE(SUM(budget), 0) FROM sys_project")
    BigDecimal sumBudget();
}

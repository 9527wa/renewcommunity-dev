package com.renewcommunity.renewcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.renewcommunity.renewcommunity.entity.SysSatisfaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 满意度评价数据访问。
 */
@Mapper
public interface SysSatisfactionMapper extends BaseMapper<SysSatisfaction> {

    @Select("SELECT COALESCE(ROUND(AVG(score), 1), 0) FROM sys_satisfaction")
    double averageScore();

    @Select("SELECT COUNT(*) FROM sys_satisfaction WHERE score = #{score}")
    long countByScore(@Param("score") Integer score);

    @Select("SELECT COUNT(*) FROM sys_satisfaction WHERE score >= #{score}")
    long countAtLeast(@Param("score") Integer score);
}

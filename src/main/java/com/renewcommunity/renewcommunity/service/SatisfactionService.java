package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.vo.SatisfactionStatsVO;
import com.renewcommunity.renewcommunity.model.vo.SatisfactionVO;

/**
 * 满意度管理服务。
 */
public interface SatisfactionService {

    PageResult<SatisfactionVO> listSatisfactions(Integer score, int pageNum, int pageSize);

    SatisfactionStatsVO getStats();
}

package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.dto.AppSatisfactionSubmitRequest;
import com.renewcommunity.renewcommunity.model.vo.SatisfactionVO;

/**
 * C 端满意度评价服务。
 */
public interface AppSatisfactionService {

    Long submit(Long userId, AppSatisfactionSubmitRequest request);

    PageResult<SatisfactionVO> listMy(Long userId, int pageNum, int pageSize);
}

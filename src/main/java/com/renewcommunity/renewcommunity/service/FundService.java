package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.dto.FundSaveRequest;
import com.renewcommunity.renewcommunity.model.vo.FundVO;

/**
 * 资金台账服务。
 */
public interface FundService {

    PageResult<FundVO> listFunds(String keyword, Integer type, Long projectId, int pageNum, int pageSize);

    Long createFund(FundSaveRequest request);

    void updateFund(Long id, FundSaveRequest request);

    void deleteFund(Long id);
}

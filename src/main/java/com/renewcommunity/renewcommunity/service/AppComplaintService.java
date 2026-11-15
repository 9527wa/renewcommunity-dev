package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.dto.AppComplaintSubmitRequest;
import com.renewcommunity.renewcommunity.model.vo.AppComplaintVO;

/**
 * C 端投诉服务。
 */
public interface AppComplaintService {

    Long submitComplaint(Long userId, AppComplaintSubmitRequest request);

    PageResult<AppComplaintVO> listMyComplaints(Long userId, int pageNum, int pageSize);
}

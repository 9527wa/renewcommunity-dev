package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.dto.ComplaintHandleRequest;
import com.renewcommunity.renewcommunity.model.dto.ComplaintSaveRequest;
import com.renewcommunity.renewcommunity.model.vo.ComplaintVO;

/**
 * 投诉管理服务。
 */
public interface ComplaintService {

    PageResult<ComplaintVO> listComplaints(String keyword, Integer status, int pageNum, int pageSize);

    Long createComplaint(ComplaintSaveRequest request);

    void updateComplaint(Long id, ComplaintSaveRequest request);

    void handleComplaint(Long id, ComplaintHandleRequest request);

    void deleteComplaint(Long id);
}

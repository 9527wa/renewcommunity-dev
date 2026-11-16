package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.dto.AppComplaintSubmitRequest;
import com.renewcommunity.renewcommunity.model.vo.AppComplaintVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * C 端投诉服务。
 */
public interface AppComplaintService {

    Long submitComplaint(Long userId, AppComplaintSubmitRequest request);

    PageResult<AppComplaintVO> listMyComplaints(Long userId, Integer status, int pageNum, int pageSize);

    AppComplaintVO getMyComplaint(Long userId, Long id);

    void uploadImages(Long userId, Long complaintId, List<MultipartFile> files);

    int remindComplaint(Long userId, Long complaintId);
}

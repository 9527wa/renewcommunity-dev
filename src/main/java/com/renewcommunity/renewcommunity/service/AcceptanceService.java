package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.dto.AcceptanceUpdateRequest;
import com.renewcommunity.renewcommunity.model.vo.AcceptanceVO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * 项目验收服务。
 */
public interface AcceptanceService {

    PageResult<AcceptanceVO> listAcceptances(Long projectId, Integer result, int pageNum, int pageSize);

    Long createAcceptance(Long projectId, String title, Integer result, LocalDate acceptDate, String conclusion,
                          List<MultipartFile> files);

    void updateAcceptance(Long id, AcceptanceUpdateRequest request);

    void deleteAcceptance(Long id);
}

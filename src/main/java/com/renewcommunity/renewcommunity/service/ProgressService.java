package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.dto.ProgressUpdateRequest;
import com.renewcommunity.renewcommunity.model.vo.ProgressVO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * 工程进度服务。
 */
public interface ProgressService {

    PageResult<ProgressVO> listProgress(Long projectId, String stage, int pageNum, int pageSize);

    Long createProgress(Long projectId, String stage, String title, String content, Integer progress,
                        LocalDate reportDate, List<MultipartFile> files);

    void updateProgress(Long id, ProgressUpdateRequest request);

    void addImages(Long id, List<MultipartFile> files);

    void deleteProgress(Long id);
}

package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.entity.SysDocument;
import com.renewcommunity.renewcommunity.entity.SysFile;
import com.renewcommunity.renewcommunity.model.vo.DocumentVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资料台账服务。
 */
public interface DocumentService {

    PageResult<DocumentVO> listDocuments(String keyword, String category, Long projectId, Integer status,
                                         int pageNum, int pageSize);

    Long uploadDocument(MultipartFile file, Long projectId, String category, String title);

    DocumentDownload getForDownload(Long id);

    void updateStatus(Long id, Integer status);

    void deleteDocument(Long id);

    /**
     * 下载所需的资料与文件记录。
     */
    record DocumentDownload(SysDocument document, SysFile file) {
    }
}

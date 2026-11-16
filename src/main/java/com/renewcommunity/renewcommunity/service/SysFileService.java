package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.entity.SysFile;
import com.renewcommunity.renewcommunity.model.vo.FileVO;

/**
 * 文件管理服务。
 */
public interface SysFileService {

    PageResult<FileVO> listFiles(String keyword, int pageNum, int pageSize);

    SysFile getFile(Long id);

    void deleteFile(Long id);
}

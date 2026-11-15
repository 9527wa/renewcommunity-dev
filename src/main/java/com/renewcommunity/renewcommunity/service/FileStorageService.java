package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.entity.SysFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务（本地磁盘）。
 */
public interface FileStorageService {

    SysFile store(MultipartFile file, Long uploaderId, String uploaderName);

    Resource load(SysFile file);

    void delete(SysFile file);
}

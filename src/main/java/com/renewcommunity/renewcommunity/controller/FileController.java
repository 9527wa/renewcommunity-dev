package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.entity.SysFile;
import com.renewcommunity.renewcommunity.model.vo.FileVO;
import com.renewcommunity.renewcommunity.service.FileStorageService;
import com.renewcommunity.renewcommunity.service.SysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * 文件管理接口。
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final SysFileService sysFileService;

    private final FileStorageService fileStorageService;

    @GetMapping("/list")
    @SaCheckPermission("file:list")
    public Result<PageResult<FileVO>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(sysFileService.listFiles(keyword, pageNum, pageSize));
    }

    /**
     * 下载文件（用于进度现场图片、验收附件等）。
     */
    @GetMapping("/{id}/download")
    @SaCheckPermission("file:download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        SysFile file = sysFileService.getFile(id);
        Resource resource = fileStorageService.load(file);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(resolveMediaType(file.getContentType()))
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("file:delete")
    public Result<Void> delete(@PathVariable Long id) {
        sysFileService.deleteFile(id);
        return Result.ok();
    }

    private MediaType resolveMediaType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception ignored) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}

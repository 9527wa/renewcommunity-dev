package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.DocumentStatusRequest;
import com.renewcommunity.renewcommunity.model.vo.DocumentVO;
import com.renewcommunity.renewcommunity.service.DocumentService;
import com.renewcommunity.renewcommunity.service.FileStorageService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * 资料台账接口。
 */
@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    private final FileStorageService fileStorageService;

    @GetMapping("/list")
    @SaCheckPermission("document:list")
    public Result<PageResult<DocumentVO>> list(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String category,
                                               @RequestParam(required = false) Long projectId,
                                               @RequestParam(required = false) Integer status,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(documentService.listDocuments(keyword, category, projectId, status, pageNum, pageSize));
    }

    @PostMapping
    @SaCheckPermission("document:upload")
    public Result<Long> upload(@RequestParam("file") MultipartFile file,
                               @RequestParam(required = false) Long projectId,
                               @RequestParam(required = false) String category,
                               @RequestParam(required = false) String title) {
        return Result.ok(documentService.uploadDocument(file, projectId, category, title));
    }

    @GetMapping("/{id}/download")
    @SaCheckPermission("document:download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        DocumentService.DocumentDownload data = documentService.getForDownload(id);
        Resource resource = fileStorageService.load(data.file());

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(data.file().getOriginalName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(resolveMediaType(data.file().getContentType()))
                .body(resource);
    }

    @PutMapping("/{id}/status")
    @SaCheckPermission("document:archive")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody DocumentStatusRequest request) {
        documentService.updateStatus(id, request.status());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("document:delete")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
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

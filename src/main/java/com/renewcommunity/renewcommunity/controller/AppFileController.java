package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysComplaintImage;
import com.renewcommunity.renewcommunity.entity.SysFile;
import com.renewcommunity.renewcommunity.mapper.SysComplaintImageMapper;
import com.renewcommunity.renewcommunity.mapper.SysComplaintMapper;
import com.renewcommunity.renewcommunity.mapper.SysProgressImageMapper;
import com.renewcommunity.renewcommunity.entity.SysProgressImage;
import com.renewcommunity.renewcommunity.service.FileStorageService;
import com.renewcommunity.renewcommunity.service.SysFileService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * C 端图片/文件访问接口。
 *
 * <p>仅允许访问工程进度现场照片，或本人投诉的图片。</p>
 */
@RestController
@RequestMapping("/api/app/file")
@RequiredArgsConstructor
public class AppFileController {

    private final SysFileService sysFileService;

    private final FileStorageService fileStorageService;

    private final SysProgressImageMapper progressImageMapper;

    private final SysComplaintImageMapper complaintImageMapper;

    private final SysComplaintMapper complaintMapper;

    @GetMapping("/{id}")
    public ResponseEntity<Resource> view(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysFile file = sysFileService.getFile(id);

        if (!canAccess(userId, id)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该文件");
        }

        Resource resource = fileStorageService.load(file);
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(resolveMediaType(file.getContentType()))
                .body(resource);
    }

    private boolean canAccess(Long userId, Long fileId) {
        long progressRefs = progressImageMapper.selectCount(Wrappers.<SysProgressImage>lambdaQuery()
                .eq(SysProgressImage::getFileId, fileId));
        if (progressRefs > 0) {
            return true;
        }

        List<SysComplaintImage> links = complaintImageMapper.selectList(Wrappers.<SysComplaintImage>lambdaQuery()
                .eq(SysComplaintImage::getFileId, fileId));
        if (links.isEmpty()) {
            return false;
        }

        List<Long> complaintIds = links.stream().map(SysComplaintImage::getComplaintId).distinct().toList();
        return complaintMapper.selectBatchIds(complaintIds).stream()
                .anyMatch(complaint -> userId.equals(complaint.getSubmitterId()));
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

package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.ProgressUpdateRequest;
import com.renewcommunity.renewcommunity.model.vo.ProgressVO;
import com.renewcommunity.renewcommunity.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

/**
 * 工程进度接口。
 */
@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/list")
    @SaCheckPermission("progress:list")
    public Result<PageResult<ProgressVO>> list(@RequestParam(required = false) Long projectId,
                                               @RequestParam(required = false) String stage,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(progressService.listProgress(projectId, stage, pageNum, pageSize));
    }

    @PostMapping
    @SaCheckPermission("progress:add")
    public Result<Long> create(@RequestParam Long projectId,
                               @RequestParam(required = false) String stage,
                               @RequestParam String title,
                               @RequestParam(required = false) String content,
                               @RequestParam(required = false) Integer progress,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate,
                               @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        return Result.ok(progressService.createProgress(projectId, stage, title, content, progress,
                reportDate, files));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("progress:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProgressUpdateRequest request) {
        progressService.updateProgress(id, request);
        return Result.ok();
    }

    @PostMapping("/{id}/images")
    @SaCheckPermission("progress:edit")
    public Result<Void> addImages(@PathVariable Long id,
                                  @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        progressService.addImages(id, files);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("progress:delete")
    public Result<Void> delete(@PathVariable Long id) {
        progressService.deleteProgress(id);
        return Result.ok();
    }
}

package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.AcceptanceUpdateRequest;
import com.renewcommunity.renewcommunity.model.vo.AcceptanceVO;
import com.renewcommunity.renewcommunity.service.AcceptanceService;
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
 * 项目验收接口。
 */
@RestController
@RequestMapping("/api/acceptance")
@RequiredArgsConstructor
public class AcceptanceController {

    private final AcceptanceService acceptanceService;

    @GetMapping("/list")
    @SaCheckPermission("acceptance:list")
    public Result<PageResult<AcceptanceVO>> list(@RequestParam(required = false) Long projectId,
                                                 @RequestParam(required = false) Integer result,
                                                 @RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(acceptanceService.listAcceptances(projectId, result, pageNum, pageSize));
    }

    @PostMapping
    @SaCheckPermission("acceptance:add")
    public Result<Long> create(@RequestParam Long projectId,
                               @RequestParam String title,
                               @RequestParam(required = false) Integer result,
                               @RequestParam(required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate acceptDate,
                               @RequestParam(required = false) String conclusion,
                               @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        return Result.ok(acceptanceService.createAcceptance(projectId, title, result, acceptDate, conclusion,
                files));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("acceptance:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AcceptanceUpdateRequest request) {
        acceptanceService.updateAcceptance(id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("acceptance:delete")
    public Result<Void> delete(@PathVariable Long id) {
        acceptanceService.deleteAcceptance(id);
        return Result.ok();
    }
}

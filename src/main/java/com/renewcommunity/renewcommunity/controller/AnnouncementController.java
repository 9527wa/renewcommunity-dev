package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.AnnouncementSaveRequest;
import com.renewcommunity.renewcommunity.model.vo.AnnouncementVO;
import com.renewcommunity.renewcommunity.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告管理接口。
 */
@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/list")
    @SaCheckPermission("announcement:list")
    public Result<PageResult<AnnouncementVO>> list(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Integer status,
                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(announcementService.listAnnouncements(keyword, status, pageNum, pageSize));
    }

    @PostMapping
    @SaCheckPermission("announcement:add")
    public Result<Long> create(@Valid @RequestBody AnnouncementSaveRequest request) {
        return Result.ok(announcementService.createAnnouncement(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("announcement:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AnnouncementSaveRequest request) {
        announcementService.updateAnnouncement(id, request);
        return Result.ok();
    }

    @PutMapping("/{id}/publish")
    @SaCheckPermission("announcement:edit")
    public Result<Void> publish(@PathVariable Long id) {
        announcementService.publishAnnouncement(id);
        return Result.ok();
    }

    @PutMapping("/{id}/offline")
    @SaCheckPermission("announcement:edit")
    public Result<Void> offline(@PathVariable Long id) {
        announcementService.offlineAnnouncement(id);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("announcement:delete")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.ok();
    }
}

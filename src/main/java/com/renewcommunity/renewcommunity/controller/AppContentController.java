package com.renewcommunity.renewcommunity.controller;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.vo.AppAnnouncementDetailVO;
import com.renewcommunity.renewcommunity.model.vo.AppAnnouncementVO;
import com.renewcommunity.renewcommunity.model.vo.AppCommunityVO;
import com.renewcommunity.renewcommunity.model.vo.AppProgressVO;
import com.renewcommunity.renewcommunity.model.vo.AppProjectVO;
import com.renewcommunity.renewcommunity.service.AppContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端公开内容接口：公告、项目、小区。
 */
@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppContentController {

    private final AppContentService appContentService;

    @GetMapping("/announcement/list")
    public Result<PageResult<AppAnnouncementVO>> listAnnouncements(@RequestParam(required = false) String keyword,
                                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(appContentService.listAnnouncements(keyword, pageNum, pageSize));
    }

    @GetMapping("/announcement/{id}")
    public Result<AppAnnouncementDetailVO> getAnnouncement(@PathVariable Long id) {
        return Result.ok(appContentService.getAnnouncement(id));
    }

    @GetMapping("/project/list")
    public Result<PageResult<AppProjectVO>> listProjects(@RequestParam(required = false) Integer status,
                                                         @RequestParam(required = false) String community,
                                                         @RequestParam(required = false) String keyword,
                                                         @RequestParam(defaultValue = "1") int pageNum,
                                                         @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(appContentService.listProjects(status, community, keyword, pageNum, pageSize));
    }

    @GetMapping("/project/{id}")
    public Result<AppProjectVO> getProject(@PathVariable Long id) {
        return Result.ok(appContentService.getProject(id));
    }

    @GetMapping("/project/{id}/progress")
    public Result<PageResult<AppProgressVO>> listProjectProgress(@PathVariable Long id,
                                                                 @RequestParam(defaultValue = "1") int pageNum,
                                                                 @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(appContentService.listProjectProgress(id, pageNum, pageSize));
    }

    @GetMapping("/community/list")
    public Result<List<AppCommunityVO>> listCommunities() {
        return Result.ok(appContentService.listCommunities());
    }
}

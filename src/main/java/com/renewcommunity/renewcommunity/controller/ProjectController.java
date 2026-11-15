package com.renewcommunity.renewcommunity.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.Result;
import com.renewcommunity.renewcommunity.model.dto.ProjectSaveRequest;
import com.renewcommunity.renewcommunity.model.vo.ProjectVO;
import com.renewcommunity.renewcommunity.service.ProjectService;
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

import java.util.List;

/**
 * 项目管理接口。
 */
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/list")
    @SaCheckPermission("project:list")
    public Result<PageResult<ProjectVO>> list(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(defaultValue = "1") int pageNum,
                                              @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(projectService.listProjects(keyword, status, pageNum, pageSize));
    }

    @GetMapping("/options")
    @SaCheckPermission("project:list")
    public Result<List<ProjectVO>> options() {
        return Result.ok(projectService.listAllProjects());
    }

    @PostMapping
    @SaCheckPermission("project:add")
    public Result<Long> create(@Valid @RequestBody ProjectSaveRequest request) {
        return Result.ok(projectService.createProject(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("project:edit")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProjectSaveRequest request) {
        projectService.updateProject(id, request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("project:delete")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.deleteProject(id);
        return Result.ok();
    }
}

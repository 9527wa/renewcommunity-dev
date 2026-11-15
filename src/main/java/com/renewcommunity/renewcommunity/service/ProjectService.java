package com.renewcommunity.renewcommunity.service;

import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.model.dto.ProjectSaveRequest;
import com.renewcommunity.renewcommunity.model.vo.ProjectVO;

import java.util.List;

/**
 * 项目管理服务。
 */
public interface ProjectService {

    PageResult<ProjectVO> listProjects(String keyword, Integer status, int pageNum, int pageSize);

    List<ProjectVO> listAllProjects();

    Long createProject(ProjectSaveRequest request);

    void updateProject(Long id, ProjectSaveRequest request);

    void deleteProject(Long id);
}

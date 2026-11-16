package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysAcceptance;
import com.renewcommunity.renewcommunity.entity.SysComplaint;
import com.renewcommunity.renewcommunity.entity.SysDocument;
import com.renewcommunity.renewcommunity.entity.SysFund;
import com.renewcommunity.renewcommunity.entity.SysProgress;
import com.renewcommunity.renewcommunity.entity.SysProject;
import com.renewcommunity.renewcommunity.mapper.SysAcceptanceMapper;
import com.renewcommunity.renewcommunity.mapper.SysComplaintMapper;
import com.renewcommunity.renewcommunity.mapper.SysDocumentMapper;
import com.renewcommunity.renewcommunity.mapper.SysFundMapper;
import com.renewcommunity.renewcommunity.mapper.SysProgressMapper;
import com.renewcommunity.renewcommunity.mapper.SysProjectMapper;
import com.renewcommunity.renewcommunity.model.dto.ProjectSaveRequest;
import com.renewcommunity.renewcommunity.model.vo.ProjectVO;
import com.renewcommunity.renewcommunity.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 项目管理实现。
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final SysProjectMapper projectMapper;

    private final SysProgressMapper progressMapper;

    private final SysAcceptanceMapper acceptanceMapper;

    private final SysFundMapper fundMapper;

    private final SysComplaintMapper complaintMapper;

    private final SysDocumentMapper documentMapper;

    @Override
    public PageResult<ProjectVO> listProjects(String keyword, Integer status, int pageNum, int pageSize) {
        Page<SysProject> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 200));
        var query = Wrappers.<SysProject>lambdaQuery();

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like(SysProject::getName, keyword)
                    .or()
                    .like(SysProject::getCommunity, keyword)
                    .or()
                    .like(SysProject::getManager, keyword));
        }

        if (status != null) {
            query.eq(SysProject::getStatus, status);
        }

        query.orderByDesc(SysProject::getId);
        Page<SysProject> result = projectMapper.selectPage(page, query);
        List<ProjectVO> list = result.getRecords().stream().map(ProjectServiceImpl::toVO).toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    public List<ProjectVO> listAllProjects() {
        return projectMapper.selectList(Wrappers.<SysProject>lambdaQuery()
                        .orderByAsc(SysProject::getId))
                .stream()
                .map(ProjectServiceImpl::toVO)
                .toList();
    }

    @Override
    public Long createProject(ProjectSaveRequest request) {
        SysProject project = new SysProject();
        apply(project, request);
        projectMapper.insert(project);

        return project.getId();
    }

    @Override
    public void updateProject(Long id, ProjectSaveRequest request) {
        requireProject(id);

        SysProject project = new SysProject();
        project.setId(id);
        apply(project, request);
        projectMapper.updateById(project);
    }

    @Override
    public void deleteProject(Long id) {
        requireProject(id);

        long progressCount = progressMapper.selectCount(Wrappers.<SysProgress>lambdaQuery()
                .eq(SysProgress::getProjectId, id));
        long acceptanceCount = acceptanceMapper.selectCount(Wrappers.<SysAcceptance>lambdaQuery()
                .eq(SysAcceptance::getProjectId, id));
        long fundCount = fundMapper.selectCount(Wrappers.<SysFund>lambdaQuery()
                .eq(SysFund::getProjectId, id));
        long complaintCount = complaintMapper.selectCount(Wrappers.<SysComplaint>lambdaQuery()
                .eq(SysComplaint::getProjectId, id));
        long documentCount = documentMapper.selectCount(Wrappers.<SysDocument>lambdaQuery()
                .eq(SysDocument::getProjectId, id));

        if (progressCount + acceptanceCount + fundCount + complaintCount + documentCount > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "项目下存在进度、验收、资金、资料或投诉记录，无法删除");
        }

        projectMapper.deleteById(id);
    }

    private void apply(SysProject project, ProjectSaveRequest request) {
        project.setName(request.name());
        project.setCommunity(defaultString(request.community()));
        project.setCategory(defaultString(request.category()));
        project.setStatus(request.status() == null ? 0 : request.status());
        project.setProgress(request.progress() == null ? 0 : request.progress());
        project.setBudget(request.budget() == null ? BigDecimal.ZERO : request.budget());
        project.setStartDate(request.startDate());
        project.setEndDate(request.endDate());
        project.setManager(defaultString(request.manager()));
        project.setConstructionUnit(defaultString(request.constructionUnit()));
        project.setDescription(defaultString(request.description()));
    }

    private void requireProject(Long id) {
        if (id == null || projectMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "项目不存在");
        }
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private static ProjectVO toVO(SysProject project) {
        return new ProjectVO(project.getId(), project.getName(), project.getCommunity(), project.getCategory(),
                project.getStatus(), project.getProgress(), project.getBudget(), project.getStartDate(),
                project.getEndDate(), project.getManager(), project.getConstructionUnit(),
                project.getDescription());
    }
}

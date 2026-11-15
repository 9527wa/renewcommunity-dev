package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysComplaint;
import com.renewcommunity.renewcommunity.entity.SysProject;
import com.renewcommunity.renewcommunity.entity.SysUser;
import com.renewcommunity.renewcommunity.mapper.SysComplaintMapper;
import com.renewcommunity.renewcommunity.mapper.SysProjectMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserMapper;
import com.renewcommunity.renewcommunity.model.dto.AppComplaintSubmitRequest;
import com.renewcommunity.renewcommunity.model.vo.AppComplaintVO;
import com.renewcommunity.renewcommunity.service.AppComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * C 端投诉实现。
 */
@Service
@RequiredArgsConstructor
public class AppComplaintServiceImpl implements AppComplaintService {

    private static final String DEFAULT_TYPE = "其他";

    private final SysComplaintMapper complaintMapper;

    private final SysProjectMapper projectMapper;

    private final SysUserMapper userMapper;

    @Override
    public Long submitComplaint(Long userId, AppComplaintSubmitRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        SysProject project = null;
        if (request.projectId() != null) {
            project = projectMapper.selectById(request.projectId());
            if (project == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "关联项目不存在");
            }
        }

        SysComplaint complaint = new SysComplaint();
        complaint.setTitle(request.title());
        complaint.setContent(request.content());
        complaint.setType(StringUtils.hasText(request.type()) ? request.type() : DEFAULT_TYPE);
        complaint.setProjectId(request.projectId());
        complaint.setSubmitterId(userId);
        complaint.setCommunity(resolveCommunity(request.community(), project, user));
        complaint.setContactName(resolveContactName(request.contactName(), user));
        complaint.setContactMobile(resolveContactMobile(request.contactMobile(), user));
        complaint.setStatus(0);
        complaintMapper.insert(complaint);

        return complaint.getId();
    }

    @Override
    public PageResult<AppComplaintVO> listMyComplaints(Long userId, int pageNum, int pageSize) {
        Page<SysComplaint> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 100));
        var query = Wrappers.<SysComplaint>lambdaQuery()
                .eq(SysComplaint::getSubmitterId, userId)
                .orderByDesc(SysComplaint::getCreatedAt)
                .orderByDesc(SysComplaint::getId);

        Page<SysComplaint> result = complaintMapper.selectPage(page, query);
        Map<Long, String> projectNames = projectNameMap(result.getRecords().stream()
                .map(SysComplaint::getProjectId)
                .filter(Objects::nonNull)
                .toList());

        List<AppComplaintVO> list = result.getRecords().stream()
                .map(item -> new AppComplaintVO(item.getId(), item.getTitle(), item.getContent(), item.getType(),
                        item.getStatus(), item.getProjectId(), projectNames.get(item.getProjectId()),
                        item.getReply(), item.getCreatedAt(), item.getHandledAt()))
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    private String resolveCommunity(String community, SysProject project, SysUser user) {
        if (StringUtils.hasText(community)) {
            return community;
        }

        if (project != null && StringUtils.hasText(project.getCommunity())) {
            return project.getCommunity();
        }

        return "";
    }

    private String resolveContactName(String contactName, SysUser user) {
        if (StringUtils.hasText(contactName)) {
            return contactName;
        }

        return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
    }

    private String resolveContactMobile(String contactMobile, SysUser user) {
        if (StringUtils.hasText(contactMobile)) {
            return contactMobile;
        }

        return user.getMobile() == null ? "" : user.getMobile();
    }

    private Map<Long, String> projectNameMap(Collection<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Map.of();
        }

        return projectMapper.selectBatchIds(projectIds).stream()
                .collect(Collectors.toMap(SysProject::getId, SysProject::getName));
    }
}

package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysComplaint;
import com.renewcommunity.renewcommunity.entity.SysComplaintImage;
import com.renewcommunity.renewcommunity.entity.SysFile;
import com.renewcommunity.renewcommunity.entity.SysProject;
import com.renewcommunity.renewcommunity.entity.SysSatisfaction;
import com.renewcommunity.renewcommunity.entity.SysUser;
import com.renewcommunity.renewcommunity.mapper.SysComplaintImageMapper;
import com.renewcommunity.renewcommunity.mapper.SysComplaintMapper;
import com.renewcommunity.renewcommunity.mapper.SysFileMapper;
import com.renewcommunity.renewcommunity.mapper.SysProjectMapper;
import com.renewcommunity.renewcommunity.mapper.SysSatisfactionMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserMapper;
import com.renewcommunity.renewcommunity.model.dto.AppComplaintSubmitRequest;
import com.renewcommunity.renewcommunity.model.vo.AppComplaintVO;
import com.renewcommunity.renewcommunity.model.vo.FileRefVO;
import com.renewcommunity.renewcommunity.service.AppComplaintService;
import com.renewcommunity.renewcommunity.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
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

    private static final String DEFAULT_KIND = "投诉";

    private static final List<String> KINDS = List.of("投诉", "建议", "咨询");

    private final SysComplaintMapper complaintMapper;

    private final SysComplaintImageMapper complaintImageMapper;

    private final SysProjectMapper projectMapper;

    private final SysUserMapper userMapper;

    private final SysSatisfactionMapper satisfactionMapper;

    private final SysFileMapper fileMapper;

    private final FileStorageService fileStorageService;

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
        complaint.setKind(normalizeKind(request.kind()));
        complaint.setProjectId(request.projectId());
        complaint.setSubmitterId(userId);
        complaint.setCommunity(resolveCommunity(request.community(), project));
        complaint.setLocation(request.location() == null ? "" : request.location().trim());
        complaint.setContactName(resolveContactName(request.contactName(), user));
        complaint.setContactMobile(resolveContactMobile(request.contactMobile(), user));
        complaint.setStatus(0);
        complaintMapper.insert(complaint);

        return complaint.getId();
    }

    @Override
    public PageResult<AppComplaintVO> listMyComplaints(Long userId, Integer status, int pageNum, int pageSize) {
        Page<SysComplaint> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 100));
        var query = Wrappers.<SysComplaint>lambdaQuery()
                .eq(SysComplaint::getSubmitterId, userId);

        if (status != null) {
            query.eq(SysComplaint::getStatus, status);
        }

        query.orderByDesc(SysComplaint::getCreatedAt).orderByDesc(SysComplaint::getId);
        Page<SysComplaint> result = complaintMapper.selectPage(page, query);
        List<SysComplaint> records = result.getRecords();
        List<Long> complaintIds = records.stream().map(SysComplaint::getId).toList();

        Map<Long, String> projectNames = projectNameMap(records.stream()
                .map(SysComplaint::getProjectId)
                .filter(Objects::nonNull)
                .toList());
        Map<Long, Integer> scores = satisfactionScores(complaintIds);
        Map<Long, List<FileRefVO>> images = loadImages(complaintIds);

        List<AppComplaintVO> list = records.stream()
                .map(item -> toVO(item, item.getProjectId() == null ? null : projectNames.get(item.getProjectId()),
                        scores.get(item.getId()),
                        images.getOrDefault(item.getId(), List.of())))
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    public AppComplaintVO getMyComplaint(Long userId, Long id) {
        SysComplaint complaint = requireOwnComplaint(userId, id);
        String projectName = complaint.getProjectId() == null
                ? null
                : projectNameMap(List.of(complaint.getProjectId())).get(complaint.getProjectId());
        Integer score = satisfactionScores(List.of(id)).get(id);
        List<FileRefVO> images = loadImages(List.of(id)).getOrDefault(id, List.of());

        return toVO(complaint, projectName, score, images);
    }

    @Override
    public int remindComplaint(Long userId, Long complaintId) {
        SysComplaint complaint = requireOwnComplaint(userId, complaintId);

        if (Integer.valueOf(2).equals(complaint.getStatus())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "投诉已办结，无需催办");
        }

        int count = (complaint.getRemindCount() == null ? 0 : complaint.getRemindCount()) + 1;
        SysComplaint update = new SysComplaint();
        update.setId(complaintId);
        update.setRemindCount(count);
        update.setLastRemindAt(LocalDateTime.now());
        complaintMapper.updateById(update);

        return count;
    }

    @Override
    @Transactional
    public void uploadImages(Long userId, Long complaintId, List<MultipartFile> files) {
        requireOwnComplaint(userId, complaintId);

        if (files == null || files.isEmpty()) {
            return;
        }

        SysUser user = userMapper.selectById(userId);
        String uploaderName = user == null
                ? "居民" + userId
                : (StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
        long existing = complaintImageMapper.selectCount(Wrappers.<SysComplaintImage>lambdaQuery()
                .eq(SysComplaintImage::getComplaintId, complaintId));
        int sort = (int) existing;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            SysFile stored = fileStorageService.store(file, userId, uploaderName);
            SysComplaintImage image = new SysComplaintImage();
            image.setComplaintId(complaintId);
            image.setFileId(stored.getId());
            image.setSort(sort++);
            complaintImageMapper.insert(image);
        }
    }

    private SysComplaint requireOwnComplaint(Long userId, Long id) {
        SysComplaint complaint = id == null ? null : complaintMapper.selectById(id);

        if (complaint == null || !userId.equals(complaint.getSubmitterId())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "投诉不存在");
        }

        return complaint;
    }

    private String resolveCommunity(String community, SysProject project) {
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
            return Collections.emptyMap();
        }

        return projectMapper.selectBatchIds(projectIds).stream()
                .collect(Collectors.toMap(SysProject::getId, SysProject::getName));
    }

    private Map<Long, Integer> satisfactionScores(Collection<Long> complaintIds) {
        if (complaintIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return satisfactionMapper.selectList(Wrappers.<SysSatisfaction>lambdaQuery()
                        .in(SysSatisfaction::getComplaintId, complaintIds))
                .stream()
                .collect(Collectors.toMap(SysSatisfaction::getComplaintId, SysSatisfaction::getScore));
    }

    private Map<Long, List<FileRefVO>> loadImages(Collection<Long> complaintIds) {
        if (complaintIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<SysComplaintImage> links = complaintImageMapper.selectList(Wrappers.<SysComplaintImage>lambdaQuery()
                .in(SysComplaintImage::getComplaintId, complaintIds)
                .orderByAsc(SysComplaintImage::getSort));
        if (links.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> fileIds = links.stream().map(SysComplaintImage::getFileId).toList();
        Map<Long, SysFile> files = fileMapper.selectBatchIds(fileIds).stream()
                .collect(Collectors.toMap(SysFile::getId, item -> item));

        return links.stream().collect(Collectors.groupingBy(SysComplaintImage::getComplaintId,
                Collectors.mapping(link -> {
                    SysFile file = files.get(link.getFileId());
                    return new FileRefVO(link.getFileId(),
                            file == null ? null : file.getOriginalName(),
                            file == null ? null : file.getContentType());
                }, Collectors.toList())));
    }

    private AppComplaintVO toVO(SysComplaint complaint, String projectName, Integer score, List<FileRefVO> images) {
        return new AppComplaintVO(complaint.getId(), complaint.getTitle(), complaint.getContent(),
                complaint.getType(), complaint.getKind(), complaint.getCommunity(), complaint.getStatus(),
                complaint.getProjectId(),
                projectName, complaint.getReply(), complaint.getCreatedAt(), complaint.getHandledAt(),
                score != null, score, images, complaint.getLocation(), complaint.getRemindCount(),
                orderNo(complaint));
    }

    private String orderNo(SysComplaint complaint) {
        if (complaint.getCreatedAt() == null) {
            return "TS" + complaint.getId();
        }

        return "TS" + complaint.getCreatedAt().toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE)
                + String.format("%03d", complaint.getId());
    }

    private String normalizeKind(String kind) {
        String value = StringUtils.hasText(kind) ? kind : DEFAULT_KIND;

        if (!KINDS.contains(value)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "事项类型不合法");
        }

        return value;
    }
}

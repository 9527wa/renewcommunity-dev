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
import com.renewcommunity.renewcommunity.mapper.SysComplaintImageMapper;
import com.renewcommunity.renewcommunity.mapper.SysComplaintMapper;
import com.renewcommunity.renewcommunity.mapper.SysFileMapper;
import com.renewcommunity.renewcommunity.mapper.SysProjectMapper;
import com.renewcommunity.renewcommunity.model.dto.ComplaintHandleRequest;
import com.renewcommunity.renewcommunity.model.dto.ComplaintSaveRequest;
import com.renewcommunity.renewcommunity.model.vo.ComplaintVO;
import com.renewcommunity.renewcommunity.model.vo.FileRefVO;
import com.renewcommunity.renewcommunity.service.ComplaintService;
import com.renewcommunity.renewcommunity.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 投诉管理实现。
 */
@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private static final String DEFAULT_TYPE = "其他";

    private static final String DEFAULT_KIND = "投诉";

    private static final List<String> KINDS = List.of("投诉", "建议", "咨询");

    private final SysComplaintMapper complaintMapper;

    private final SysComplaintImageMapper complaintImageMapper;

    private final SysProjectMapper projectMapper;

    private final SysFileMapper fileMapper;

    private final FileStorageService fileStorageService;

    @Override
    public PageResult<ComplaintVO> listComplaints(String keyword, Integer status, int pageNum, int pageSize) {
        Page<SysComplaint> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 200));
        var query = Wrappers.<SysComplaint>lambdaQuery();

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like(SysComplaint::getTitle, keyword)
                    .or()
                    .like(SysComplaint::getContent, keyword)
                    .or()
                    .like(SysComplaint::getCommunity, keyword));
        }

        if (status != null) {
            query.eq(SysComplaint::getStatus, status);
        }

        query.orderByDesc(SysComplaint::getCreatedAt).orderByDesc(SysComplaint::getId);
        Page<SysComplaint> result = complaintMapper.selectPage(page, query);
        List<SysComplaint> records = result.getRecords();
        Map<Long, String> projectNames = projectNameMap(records.stream()
                .map(SysComplaint::getProjectId)
                .filter(Objects::nonNull)
                .toList());
        Map<Long, List<FileRefVO>> images = loadImages(records.stream().map(SysComplaint::getId).toList());

        List<ComplaintVO> list = records.stream()
                .map(item -> toVO(item, item.getProjectId() == null ? null : projectNames.get(item.getProjectId()),
                        images.getOrDefault(item.getId(), List.of())))
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    public Long createComplaint(ComplaintSaveRequest request) {
        validateProject(request.projectId());

        SysComplaint complaint = new SysComplaint();
        apply(complaint, request);
        complaint.setStatus(0);
        complaintMapper.insert(complaint);

        return complaint.getId();
    }

    @Override
    public void updateComplaint(Long id, ComplaintSaveRequest request) {
        requireComplaint(id);
        validateProject(request.projectId());

        SysComplaint complaint = new SysComplaint();
        complaint.setId(id);
        apply(complaint, request);
        complaintMapper.updateById(complaint);
    }

    @Override
    public void handleComplaint(Long id, ComplaintHandleRequest request) {
        requireComplaint(id);

        SysComplaint complaint = new SysComplaint();
        complaint.setId(id);
        complaint.setStatus(request.status());
        complaint.setReply(defaultString(request.reply()));
        complaint.setHandledAt(LocalDateTime.now());
        complaintMapper.updateById(complaint);
    }

    @Override
    @Transactional
    public void deleteComplaint(Long id) {
        requireComplaint(id);

        List<SysComplaintImage> images = complaintImageMapper.selectList(Wrappers.<SysComplaintImage>lambdaQuery()
                .eq(SysComplaintImage::getComplaintId, id));
        images.forEach(image -> fileStorageService.delete(fileMapper.selectById(image.getFileId())));

        complaintImageMapper.delete(Wrappers.<SysComplaintImage>lambdaQuery()
                .eq(SysComplaintImage::getComplaintId, id));
        complaintMapper.deleteById(id);
    }

    private void apply(SysComplaint complaint, ComplaintSaveRequest request) {
        complaint.setTitle(request.title());
        complaint.setContent(defaultString(request.content()));
        complaint.setCommunity(defaultString(request.community()));
        complaint.setLocation(defaultString(request.location()));
        complaint.setType(StringUtils.hasText(request.type()) ? request.type() : DEFAULT_TYPE);
        complaint.setKind(normalizeKind(request.kind()));
        complaint.setProjectId(request.projectId());
        complaint.setContactName(defaultString(request.contactName()));
        complaint.setContactMobile(defaultString(request.contactMobile()));
    }

    private void validateProject(Long projectId) {
        if (projectId != null && projectMapper.selectById(projectId) == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "关联项目不存在");
        }
    }

    private void requireComplaint(Long id) {
        if (id == null || complaintMapper.selectById(id) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "投诉不存在");
        }
    }

    private Map<Long, String> projectNameMap(Collection<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return projectMapper.selectBatchIds(projectIds).stream()
                .collect(Collectors.toMap(SysProject::getId, SysProject::getName));
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

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private static ComplaintVO toVO(SysComplaint complaint, String projectName, List<FileRefVO> images) {
        return new ComplaintVO(complaint.getId(), complaint.getTitle(), complaint.getContent(),
                complaint.getCommunity(), complaint.getType(), complaint.getKind(), complaint.getStatus(),
                complaint.getProjectId(), projectName, complaint.getContactName(), complaint.getContactMobile(),
                complaint.getReply(), complaint.getHandledAt(), complaint.getCreatedAt(), images,
                complaint.getLocation(), complaint.getRemindCount(), orderNo(complaint));
    }

    private static String orderNo(SysComplaint complaint) {
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

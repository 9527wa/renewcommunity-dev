package com.renewcommunity.renewcommunity.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysDocument;
import com.renewcommunity.renewcommunity.entity.SysFile;
import com.renewcommunity.renewcommunity.entity.SysProject;
import com.renewcommunity.renewcommunity.entity.SysUser;
import com.renewcommunity.renewcommunity.mapper.SysDocumentMapper;
import com.renewcommunity.renewcommunity.mapper.SysFileMapper;
import com.renewcommunity.renewcommunity.mapper.SysProjectMapper;
import com.renewcommunity.renewcommunity.mapper.SysUserMapper;
import com.renewcommunity.renewcommunity.model.vo.DocumentVO;
import com.renewcommunity.renewcommunity.service.DocumentService;
import com.renewcommunity.renewcommunity.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 资料台账实现。
 */
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private static final String DEFAULT_CATEGORY = "其他";

    private static final int MAX_TITLE_LENGTH = 200;

    private final SysDocumentMapper documentMapper;

    private final SysFileMapper fileMapper;

    private final SysProjectMapper projectMapper;

    private final SysUserMapper userMapper;

    private final FileStorageService fileStorageService;

    @Override
    public PageResult<DocumentVO> listDocuments(String keyword, String category, Long projectId, Integer status,
                                                int pageNum, int pageSize) {
        Page<SysDocument> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 200));
        var query = Wrappers.<SysDocument>lambdaQuery();

        if (StringUtils.hasText(keyword)) {
            query.like(SysDocument::getTitle, keyword);
        }

        if (StringUtils.hasText(category)) {
            query.eq(SysDocument::getCategory, category);
        }

        if (projectId != null) {
            query.eq(SysDocument::getProjectId, projectId);
        }

        if (status != null) {
            query.eq(SysDocument::getStatus, status);
        }

        query.orderByDesc(SysDocument::getCreatedAt).orderByDesc(SysDocument::getId);
        Page<SysDocument> result = documentMapper.selectPage(page, query);
        List<SysDocument> records = result.getRecords();

        Map<Long, SysFile> files = idMap(records.stream().map(SysDocument::getFileId).filter(Objects::nonNull).toList());
        Map<Long, String> projectNames = projectNameMap(records.stream()
                .map(SysDocument::getProjectId)
                .filter(Objects::nonNull)
                .toList());

        List<DocumentVO> list = records.stream()
                .map(item -> toVO(item, files.get(item.getFileId()), projectNames.get(item.getProjectId())))
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    public Long uploadDocument(MultipartFile file, Long projectId, String category, String title) {
        validateProject(projectId);

        Long uploaderId = StpUtil.getLoginIdAsLong();
        SysFile storedFile = fileStorageService.store(file, uploaderId, resolveUploaderName(uploaderId));

        SysDocument document = new SysDocument();
        document.setProjectId(projectId);
        document.setCategory(StringUtils.hasText(category) ? category : DEFAULT_CATEGORY);
        String docTitle = StringUtils.hasText(title) ? title : storedFile.getOriginalName();
        document.setTitle(docTitle.length() > MAX_TITLE_LENGTH ? docTitle.substring(0, MAX_TITLE_LENGTH) : docTitle);
        document.setFileId(storedFile.getId());
        document.setStatus(0);
        documentMapper.insert(document);

        return document.getId();
    }

    @Override
    public DocumentDownload getForDownload(Long id) {
        SysDocument document = requireDocument(id);
        SysFile file = fileMapper.selectById(document.getFileId());

        if (file == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件记录不存在");
        }

        return new DocumentDownload(document, file);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        requireDocument(id);

        SysDocument document = new SysDocument();
        document.setId(id);
        document.setStatus(status);
        documentMapper.updateById(document);
    }

    @Override
    public void deleteDocument(Long id) {
        SysDocument document = requireDocument(id);
        SysFile file = fileMapper.selectById(document.getFileId());

        documentMapper.deleteById(id);
        fileStorageService.delete(file);
    }

    private void validateProject(Long projectId) {
        if (projectId != null && projectMapper.selectById(projectId) == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "关联项目不存在");
        }
    }

    private String resolveUploaderName(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return "用户" + userId;
        }

        return StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
    }

    private SysDocument requireDocument(Long id) {
        SysDocument document = id == null ? null : documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "资料不存在");
        }

        return document;
    }

    private Map<Long, SysFile> idMap(Collection<Long> fileIds) {
        if (fileIds.isEmpty()) {
            return Map.of();
        }

        return fileMapper.selectBatchIds(fileIds).stream()
                .collect(Collectors.toMap(SysFile::getId, item -> item));
    }

    private Map<Long, String> projectNameMap(Collection<Long> projectIds) {
        if (projectIds.isEmpty()) {
            return Map.of();
        }

        return projectMapper.selectBatchIds(projectIds).stream()
                .collect(Collectors.toMap(SysProject::getId, SysProject::getName));
    }

    private static DocumentVO toVO(SysDocument document, SysFile file, String projectName) {
        return new DocumentVO(document.getId(), document.getProjectId(), projectName, document.getCategory(),
                document.getTitle(), file == null ? null : file.getOriginalName(),
                file == null ? null : file.getSize(), file == null ? null : file.getContentType(),
                file == null ? null : file.getUploaderName(), document.getStatus(), document.getCreatedAt());
    }
}

package com.renewcommunity.renewcommunity.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.renewcommunity.renewcommunity.common.api.PageResult;
import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysAcceptanceFile;
import com.renewcommunity.renewcommunity.entity.SysComplaintImage;
import com.renewcommunity.renewcommunity.entity.SysDocument;
import com.renewcommunity.renewcommunity.entity.SysFile;
import com.renewcommunity.renewcommunity.entity.SysProgressImage;
import com.renewcommunity.renewcommunity.mapper.SysAcceptanceFileMapper;
import com.renewcommunity.renewcommunity.mapper.SysComplaintImageMapper;
import com.renewcommunity.renewcommunity.mapper.SysDocumentMapper;
import com.renewcommunity.renewcommunity.mapper.SysFileMapper;
import com.renewcommunity.renewcommunity.mapper.SysProgressImageMapper;
import com.renewcommunity.renewcommunity.model.vo.FileVO;
import com.renewcommunity.renewcommunity.service.FileStorageService;
import com.renewcommunity.renewcommunity.service.SysFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 文件管理实现。
 */
@Service
@RequiredArgsConstructor
public class SysFileServiceImpl implements SysFileService {

    private final SysFileMapper fileMapper;

    private final SysDocumentMapper documentMapper;

    private final SysProgressImageMapper progressImageMapper;

    private final SysAcceptanceFileMapper acceptanceFileMapper;

    private final SysComplaintImageMapper complaintImageMapper;

    private final FileStorageService fileStorageService;

    @Override
    public PageResult<FileVO> listFiles(String keyword, int pageNum, int pageSize) {
        Page<SysFile> page = new Page<>(Math.max(pageNum, 1), Math.min(Math.max(pageSize, 1), 200));
        var query = Wrappers.<SysFile>lambdaQuery();

        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like(SysFile::getOriginalName, keyword)
                    .or()
                    .like(SysFile::getUploaderName, keyword));
        }

        query.orderByDesc(SysFile::getCreatedAt).orderByDesc(SysFile::getId);
        Page<SysFile> result = fileMapper.selectPage(page, query);
        List<FileVO> list = result.getRecords().stream()
                .map(file -> new FileVO(file.getId(), file.getOriginalName(), file.getContentType(),
                        file.getSize(), file.getUploaderName(), file.getCreatedAt()))
                .toList();

        return new PageResult<>(result.getTotal(), list);
    }

    @Override
    public SysFile getFile(Long id) {
        SysFile file = id == null ? null : fileMapper.selectById(id);
        if (file == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }

        return file;
    }

    @Override
    public void deleteFile(Long id) {
        SysFile file = getFile(id);

        long documentRefs = documentMapper.selectCount(Wrappers.<SysDocument>lambdaQuery()
                .eq(SysDocument::getFileId, id));
        long progressRefs = progressImageMapper.selectCount(Wrappers.<SysProgressImage>lambdaQuery()
                .eq(SysProgressImage::getFileId, id));
        long acceptanceRefs = acceptanceFileMapper.selectCount(Wrappers.<SysAcceptanceFile>lambdaQuery()
                .eq(SysAcceptanceFile::getFileId, id));
        long complaintRefs = complaintImageMapper.selectCount(Wrappers.<SysComplaintImage>lambdaQuery()
                .eq(SysComplaintImage::getFileId, id));

        if (documentRefs + progressRefs + acceptanceRefs + complaintRefs > 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "文件正在被资料台账、工程进度、项目验收或投诉记录引用，无法删除");
        }

        fileStorageService.delete(file);
    }
}

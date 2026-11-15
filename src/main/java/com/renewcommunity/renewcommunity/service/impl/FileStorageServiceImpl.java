package com.renewcommunity.renewcommunity.service.impl;

import com.renewcommunity.renewcommunity.common.api.ResultCode;
import com.renewcommunity.renewcommunity.common.exception.BusinessException;
import com.renewcommunity.renewcommunity.entity.SysFile;
import com.renewcommunity.renewcommunity.mapper.SysFileMapper;
import com.renewcommunity.renewcommunity.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地磁盘文件存储实现。
 */
@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    private static final int MAX_ORIGINAL_NAME_LENGTH = 200;

    private final SysFileMapper fileMapper;

    private final Path root;

    public FileStorageServiceImpl(SysFileMapper fileMapper,
                                  @Value("${app.file.storage-path:}") String storagePath) throws IOException {
        this.fileMapper = fileMapper;
        String path = StringUtils.hasText(storagePath)
                ? storagePath
                : System.getProperty("user.home") + "/renewcommunity/uploads";
        this.root = Path.of(path).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
        log.info("文件存储目录: {}", this.root);
    }

    @Override
    public SysFile store(MultipartFile file, Long uploaderId, String uploaderName) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件大小不能超过 50MB");
        }

        String originalName = sanitizeOriginalName(file.getOriginalFilename());
        String extension = resolveExtension(originalName);
        String storedName = UUID.randomUUID().toString().replace("-", "") + extension;
        Path target = resolveStoredPath(storedName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("保存文件失败: {}", originalName, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件保存失败");
        }

        SysFile record = new SysFile();
        record.setOriginalName(originalName);
        record.setStoredName(storedName);
        record.setContentType(file.getContentType() == null ? "" : file.getContentType());
        record.setSize(file.getSize());
        record.setUploaderId(uploaderId);
        record.setUploaderName(uploaderName == null ? "" : uploaderName);
        fileMapper.insert(record);

        return record;
    }

    @Override
    public Resource load(SysFile file) {
        Path path = resolveStoredPath(file.getStoredName());

        if (!Files.exists(path) || !Files.isReadable(path)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在或已被删除");
        }

        return new FileSystemResource(path);
    }

    @Override
    public void delete(SysFile file) {
        if (file == null) {
            return;
        }

        fileMapper.deleteById(file.getId());

        try {
            Files.deleteIfExists(resolveStoredPath(file.getStoredName()));
        } catch (IOException e) {
            log.warn("删除文件失败: {}", file.getStoredName(), e);
        }
    }

    private Path resolveStoredPath(String storedName) {
        Path path = root.resolve(storedName).normalize();

        if (!path.startsWith(root)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法的文件路径");
        }

        return path;
    }

    private String sanitizeOriginalName(String originalName) {
        String name = StringUtils.hasText(originalName) ? originalName : "file";

        try {
            name = Path.of(name).getFileName().toString();
        } catch (Exception ignored) {
            // 文件名非法时使用默认名称
            name = "file";
        }

        return name.length() > MAX_ORIGINAL_NAME_LENGTH ? name.substring(0, MAX_ORIGINAL_NAME_LENGTH) : name;
    }

    private String resolveExtension(String originalName) {
        int index = originalName.lastIndexOf('.');
        if (index < 0 || index == originalName.length() - 1) {
            return "";
        }

        String extension = originalName.substring(index).toLowerCase();
        return extension.matches("\\.\\w{1,10}") ? extension : "";
    }
}

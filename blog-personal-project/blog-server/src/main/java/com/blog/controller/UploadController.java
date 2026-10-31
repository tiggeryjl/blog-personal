package com.blog.controller;

import com.blog.exception.CustomException;
import com.blog.result.Result;
import com.blog.utils.AliyunOSSOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024L;
    private static final int MIN_AVATAR_DIMENSION = 32;
    private static final int MAX_AVATAR_DIMENSION = 4096;
    private static final long MAX_AVATAR_PIXELS = 16_777_216L;
    private static final Set<String> AVATAR_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    private static final Set<String> AVATAR_FORMATS = Set.of("jpeg", "png", "gif");

    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;

    @PostMapping("/upload")
    @PreAuthorize("@adminAccess.hasAccess(authentication)")
    public Result upload(MultipartFile file) throws Exception {
        log.info("文件上传: {}", file.getOriginalFilename());

        //将文件交给OSS存储管理
        String url = aliyunOSSOperator.upload(file.getBytes(), file.getOriginalFilename());
        log.info("文件上传OSS, url: {}", url);

        return Result.success(url);
    }

    /**
     * 用户头像上传：校验大小、扩展名和真实图片格式
     */
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(MultipartFile file) throws Exception {
        ValidatedAvatar avatar = validateAvatar(file);
        String url = aliyunOSSOperator.upload(avatar.content(), "avatar." + avatar.extension());
        log.info("头像上传成功，用户ID：{}", com.blog.context.BaseContext.getCurrentId());
        return Result.success(url);
    }

    private ValidatedAvatar validateAvatar(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new CustomException("请选择头像文件");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new CustomException("头像大小不能超过2MB");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
        );
        int dot = originalFilename.lastIndexOf('.');
        String extension = dot >= 0
                ? originalFilename.substring(dot + 1).toLowerCase(Locale.ROOT)
                : "";
        if (!AVATAR_EXTENSIONS.contains(extension)) {
            throw new CustomException("头像仅支持 JPG、PNG、GIF 格式");
        }

        byte[] content = file.getBytes();
        if (content.length == 0 || content.length > MAX_AVATAR_SIZE) {
            throw new CustomException("头像大小不能超过2MB");
        }

        String detectedFormat;
        try (ImageInputStream imageInput = ImageIO.createImageInputStream(
                new ByteArrayInputStream(content))) {
            if (imageInput == null) {
                throw new CustomException("头像文件内容无效");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new CustomException("头像不是有效图片");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);
                detectedFormat = reader.getFormatName().toLowerCase(Locale.ROOT);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < MIN_AVATAR_DIMENSION || height < MIN_AVATAR_DIMENSION) {
                    throw new CustomException("头像尺寸不能小于32×32像素");
                }
                if (width > MAX_AVATAR_DIMENSION || height > MAX_AVATAR_DIMENSION
                        || (long) width * height > MAX_AVATAR_PIXELS) {
                    throw new CustomException("头像尺寸不能超过4096×4096像素");
                }
                if (reader.read(0) == null) {
                    throw new CustomException("头像图片内容无效");
                }
            } finally {
                reader.dispose();
            }
        }

        if (!AVATAR_FORMATS.contains(detectedFormat)) {
            throw new CustomException("头像仅支持 JPG、PNG、GIF 格式");
        }
        boolean formatMatchesExtension = switch (detectedFormat) {
            case "jpeg" -> "jpg".equals(extension) || "jpeg".equals(extension);
            case "png" -> "png".equals(extension);
            case "gif" -> "gif".equals(extension);
            default -> false;
        };
        if (!formatMatchesExtension) {
            throw new CustomException("头像扩展名与图片实际格式不一致");
        }

        String normalizedExtension = "jpeg".equals(detectedFormat) ? "jpg" : detectedFormat;
        return new ValidatedAvatar(content, normalizedExtension);
    }

    private record ValidatedAvatar(byte[] content, String extension) {
    }
}

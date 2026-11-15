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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    // 明确禁止的“可执行/可解析”类型：这些一旦被浏览器直接打开就是 XSS 或挂马
    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "html", "htm", "xhtml", "shtml", "hta", "xht",
            "svg", "svgz", "xml", "xsl", "xslt", "mht", "mhtml",
            "js", "mjs", "jsx", "ts", "css",
            "jsp", "jspx", "php", "asp", "aspx", "cgi", "pl",
            "exe", "dll", "bat", "cmd", "sh", "ps1", "vbs", "scr", "com", "pif", "jar", "war", "class", "swf",
            "apk", "dmg", "msi", "iso"
    );

    // 分类大小上限限制是 50MB
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024L;
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024L;
    private static final long MAX_AUDIO_SIZE = 20 * 1024 * 1024L;
    private static final long MAX_OTHER_SIZE = 50 * 1024 * 1024L;

    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp", "ico");
    private static final Set<String> VIDEO_EXT = Set.of("mp4", "webm", "ogg", "ogv", "mov", "m4v", "avi", "mkv", "flv", "wmv");
    private static final Set<String> AUDIO_EXT = Set.of("mp3", "m4a", "aac", "wav", "flac", "ogg", "wma", "amr");
    private static final Set<String> DOC_EXT = Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "csv", "md", "txt", "zip", "rar", "7z");

    /** 汇总白名单 */
    private static final Set<String> ALLOWED_EXT = new HashSet<>() {{
        addAll(IMAGE_EXT); addAll(VIDEO_EXT); addAll(AUDIO_EXT); addAll(DOC_EXT);
    }};

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
        if (file == null || file.isEmpty()) {
            throw new CustomException("文件为空请选择文件");
        }

        String name = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        int dot = name.lastIndexOf('.');
        String ext = dot >= 0 ? name.substring(dot + 1).toLowerCase(Locale.ROOT) : "";

        // 1. 无扩展名一律拒绝
        if (ext.isEmpty() || !ALLOWED_EXT.contains(ext)) {
            throw new CustomException("不支持的文件类型：" + (ext.isEmpty() ? "无扩展名" : ext));
        }

        // 2. 危险类型直接拒绝
//        if (BLOCKED_EXTENSIONS.contains(ext)) {
//            throw new CustomException("出于安全考虑，不支持上传该类型文件");
//        }

        // 3. 按类型卡大小
        long maxSize = IMAGE_EXT.contains(ext) ? MAX_IMAGE_SIZE
                : VIDEO_EXT.contains(ext) ? MAX_VIDEO_SIZE
                : AUDIO_EXT.contains(ext) ? MAX_AUDIO_SIZE
                : MAX_OTHER_SIZE;
        if (file.getSize() > maxSize) {
            throw new CustomException("文件超过大小上限 " + (maxSize / 1024 / 1024) + "MB");
        }

        // 4. 图片额外做真实格式校验（防止 .jpg 里塞 HTML）
        if (IMAGE_EXT.contains(ext)) {
            validateRealImage(file.getBytes());
        }

        log.info("文件上传: {}", name);
        //将文件交给OSS存储管理
        String url = aliyunOSSOperator.upload(file.getBytes(), name);
        log.info("文件上传OSS, url: {}", url);

        return Result.success(url);
    }

    private void validateRealImage(byte[] content) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            if (iis == null || !ImageIO.getImageReaders(iis).hasNext()) {
                throw new CustomException("文件内容不是有效图片");
            }
        }
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

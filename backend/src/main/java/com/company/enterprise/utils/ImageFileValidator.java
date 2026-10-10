package com.company.enterprise.utils;

import java.util.Locale;
import java.util.Set;

/**
 * 图片上传的纯函数校验工具，避免在 Controller 中散落判断逻辑，同时便于单元测试。
 */
public final class ImageFileValidator {

    /** 单个图片大小上限，需与 application.yml 的 spring.servlet.multipart.max-file-size 保持一致。 */
    public static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    public static boolean isAllowed(String originalFilename, String contentType, long size) {
        return isAllowedExtension(originalFilename)
                && isAllowedContentType(contentType)
                && isAllowedSize(size);
    }

    public static boolean isAllowedExtension(String originalFilename) {
        String extension = extensionOf(originalFilename);
        return extension != null && ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * 部分 HTTP 客户端不发送 Content-Type，此时放行；扩展名白名单仍是唯一的硬性门槛。
     */
    public static boolean isAllowedContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return true;
        }
        return ALLOWED_CONTENT_TYPES.contains(contentType.trim().toLowerCase(Locale.ROOT));
    }

    public static String resolveContentType(String originalFilename, String contentType) {
        if (contentType != null && !contentType.isBlank()) {
            String normalized = contentType.trim().toLowerCase(Locale.ROOT);
            return ALLOWED_CONTENT_TYPES.contains(normalized) ? normalized : null;
        }
        String extension = extensionOf(originalFilename);
        return switch (extension == null ? "" : extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> null;
        };
    }

    public static boolean isAllowedSize(long size) {
        return size > 0 && size <= MAX_IMAGE_SIZE_BYTES;
    }

    /**
     * 取小写扩展名；无扩展名或文件名为空时返回 null，调用方需自行兜底。
     */
    public static String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return null;
        }
        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return null;
        }
        return originalFilename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    public static String describeLimit() {
        return "仅支持 jpg/jpeg/png/gif/webp 格式，且不超过 5MB";
    }

    private ImageFileValidator() {
    }
}

package com.company.enterprise.controller;

import com.company.enterprise.common.Result;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.utils.AliyunOSSOperator;
import com.company.enterprise.utils.ImageFileValidator;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileControllerTest {

    private static final String OSS_URL = "https://example-bucket.oss-cn-beijing.aliyuncs.com/2026/09/abc.png";

    private final AliyunOSSOperator operator = mock(AliyunOSSOperator.class);
    private final FileController controller = new FileController(operator);

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.uploadImage(file));

        assertEquals(400, exception.getCode());
        assertEquals("请选择要上传的图片", exception.getMessage());
    }

    @Test
    void rejectsUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile("file", "payload.svg", "image/svg+xml",
                "<svg/>".getBytes(StandardCharsets.UTF_8));

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.uploadImage(file));

        assertEquals(400, exception.getCode());
    }

    @Test
    void rejectsUnsupportedContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "application/octet-stream",
                "image".getBytes(StandardCharsets.UTF_8));

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.uploadImage(file));

        assertEquals(400, exception.getCode());
    }

    @Test
    void rejectsOversizedImage() {
        byte[] content = new byte[(int) ImageFileValidator.MAX_IMAGE_SIZE_BYTES + 1];
        MockMultipartFile file = new MockMultipartFile("file", "big.png", "image/png", content);

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.uploadImage(file));

        assertEquals(400, exception.getCode());
    }

    @Test
    void returnsOssUrlForValidImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "Avatar.PNG", "image/png",
                "image".getBytes(StandardCharsets.UTF_8));
        when(operator.upload(any(byte[].class), eq("Avatar.PNG"), eq("image/png"))).thenReturn(OSS_URL);

        Result<String> result = controller.uploadImage(file);

        assertEquals(200, result.getCode());
        assertEquals(OSS_URL, result.getData());
        org.mockito.Mockito.verify(operator).upload(any(byte[].class), eq("Avatar.PNG"), eq("image/png"));
    }

    @Test
    void infersContentTypeWhenClientDoesNotSendOne() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", null,
                "image".getBytes(StandardCharsets.UTF_8));
        when(operator.upload(any(byte[].class), eq("avatar.jpg"), eq("image/jpeg"))).thenReturn(OSS_URL);

        Result<String> result = controller.uploadImage(file);

        assertEquals(200, result.getCode());
        org.mockito.Mockito.verify(operator).upload(any(byte[].class), eq("avatar.jpg"), eq("image/jpeg"));
    }

    @Test
    void wrapsOssFailureAsServerError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png",
                "image".getBytes(StandardCharsets.UTF_8));
        when(operator.upload(any(byte[].class), eq("avatar.png"), eq("image/png")))
                .thenThrow(new IllegalStateException("missing credentials"));

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.uploadImage(file));

        assertEquals(500, exception.getCode());
        assertEquals("图片上传失败，请稍后重试", exception.getMessage());
    }
}

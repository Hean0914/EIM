package com.company.enterprise.controller;

import com.company.enterprise.common.Result;
import com.company.enterprise.common.annotation.Audit;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.utils.AliyunOSSOperator;
import com.company.enterprise.utils.ImageFileValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final AliyunOSSOperator aliyunOSSOperator;

    public FileController(AliyunOSSOperator aliyunOSSOperator) {
        this.aliyunOSSOperator = aliyunOSSOperator;
    }

    /**
     * 上传图片到阿里云 OSS，返回可直接访问的图片地址。所有登录用户可用。
     */
    @PostMapping("/upload")
    @Audit(module = "FILE", action = "UPLOAD_IMAGE", targetType = "FILE")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的图片");
        }
        String originalFilename = file.getOriginalFilename();
        if (!ImageFileValidator.isAllowed(originalFilename, file.getContentType(), file.getSize())) {
            throw new BusinessException(400, "图片不符合要求：" + ImageFileValidator.describeLimit());
        }
        try {
            String contentType = ImageFileValidator.resolveContentType(originalFilename, file.getContentType());
            return Result.success(aliyunOSSOperator.upload(file.getBytes(), originalFilename, contentType));
        } catch (IOException exception) {
            throw new BusinessException(400, "读取上传文件失败，请重试");
        } catch (Exception exception) {
            log.warn("OSS image upload failed: {}", exception.getClass().getSimpleName());
            throw new BusinessException(500, "图片上传失败，请稍后重试");
        }
    }
}

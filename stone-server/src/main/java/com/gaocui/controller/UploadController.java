package com.gaocui.controller;

import com.gaocui.common.Result;
import com.gaocui.service.QwenUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/api")
public class UploadController {

    private final QwenUploadService uploadService;

    public UploadController(QwenUploadService uploadService) {
        this.uploadService = uploadService;
    }

    // Multipart文件上传
    @PostMapping("/upload")
    public Result<?> upload(@RequestParam(value = "file", required = false) MultipartFile file,
                            @RequestParam(defaultValue = "qwen-vl-plus") String model) {
        if (file == null || file.isEmpty()) {
            return Result.fail(400, "文件为空");
        }
        return doUpload(model, file);
    }

    // Base64上传
    @PostMapping("/upload/base64")
    public Result<?> uploadBase64(@RequestBody Map<String, String> body) {
        String base64 = body.get("image");
        String model = body.getOrDefault("model", "qwen-vl-plus");
        if (base64 == null || base64.isEmpty()) return Result.fail(400, "图片数据为空");

        try {
            // data:image/png;base64,xxxx → 取base64部分
            if (base64.contains(",")) base64 = base64.substring(base64.indexOf(",") + 1);
            byte[] bytes = Base64.getDecoder().decode(base64);
            String fileName = UUID.randomUUID().toString() + ".png";
            String ossUrl = uploadService.upload(model, bytes, fileName);
            Map<String, Object> result = new HashMap<>();
            result.put("url", ossUrl);
            result.put("expireIn", "48h");
            return Result.ok(result);
        } catch (Exception e) {
            log.error("base64上传失败", e);
            return Result.fail(500, "上传失败: " + e.getMessage());
        }
    }

    private Result<?> doUpload(String model, MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String ext = ".png";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + ext;
            String ossUrl = uploadService.upload(model, file.getBytes(), fileName);
            Map<String, Object> result = new HashMap<>();
            result.put("url", ossUrl);
            result.put("expireIn", "48h");
            return Result.ok(result);
        } catch (Exception e) {
            log.error("上传失败", e);
            return Result.fail(500, "上传失败: " + e.getMessage());
        }
    }
}

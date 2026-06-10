package com.iwan.blog.controller;

import com.iwan.blog.vo.ResponseVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private String uploadDir;
    private static final String AVATAR_DIR = "avatar/";

    @PostConstruct
    public void init() {
        // 使用项目根目录的绝对路径
        String projectDir = System.getProperty("user.dir");
        uploadDir = projectDir + File.separator + "uploads" + File.separator;
        
        // 确保上传目录存在
        try {
            Path path = Paths.get(uploadDir + AVATAR_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            System.out.println("上传目录: " + uploadDir + AVATAR_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/avatar")
    public ResponseVO<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseVO.error("请选择要上传的文件");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseVO.error("请上传图片文件");
        }

        // 检查文件大小（最大5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseVO.error("图片大小不能超过5MB");
        }

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = ".png";
            if (originalFilename != null && originalFilename.contains(".")) {
                String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
                if (ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") || ext.equals(".gif")) {
                    extension = ext;
                }
            }
            String newFilename = UUID.randomUUID().toString() + extension;
            String filePath = uploadDir + AVATAR_DIR + newFilename;

            // 确保目录存在
            File dest = new File(filePath);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }

            // 保存文件
            file.transferTo(dest);

            // 返回文件URL
            String fileUrl = "/uploads/avatar/" + newFilename;
            Map<String, Object> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", newFilename);

            return ResponseVO.success(result);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseVO.error("文件上传失败: " + e.getMessage());
        }
    }
}

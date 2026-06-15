package com.iwan.blog.controller;

import com.iwan.blog.vo.ResponseVO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private static final String IMAGE_DIR = "images/";
    private static final String VIDEO_DIR = "videos/";
    private static final String AUDIO_DIR = "audios/";

    @PostConstruct
    public void init() {
        String projectDir = System.getProperty("user.dir");
        uploadDir = projectDir + File.separator + "uploads" + File.separator;
        
        try {
            Path avatarPath = Paths.get(uploadDir + AVATAR_DIR);
            if (!Files.exists(avatarPath)) Files.createDirectories(avatarPath);
            Path imagePath = Paths.get(uploadDir + IMAGE_DIR);
            if (!Files.exists(imagePath)) Files.createDirectories(imagePath);
            Path videoPath = Paths.get(uploadDir + VIDEO_DIR);
            if (!Files.exists(videoPath)) Files.createDirectories(videoPath);
            Path audioPath = Paths.get(uploadDir + AUDIO_DIR);
            if (!Files.exists(audioPath)) Files.createDirectories(audioPath);
            System.out.println("上传目录初始化完成: " + uploadDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/avatar")
    public ResponseVO<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseVO.error("请选择要上传的文件");
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseVO.error("请上传图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseVO.error("图片大小不能超过5MB");
        }

        try {
            String extension = getExtension(file.getOriginalFilename(), ".png");
            String newFilename = UUID.randomUUID().toString() + extension;
            String filePath = uploadDir + AVATAR_DIR + newFilename;
            
            File dest = new File(filePath);
            if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
            file.transferTo(dest);

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

    @PostMapping("/image")
    public ResponseVO<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseVO.error("请选择要上传的文件");
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseVO.error("请上传图片文件");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            return ResponseVO.error("图片大小不能超过10MB");
        }

        try {
            String extension = getExtension(file.getOriginalFilename(), ".png");
            String newFilename = UUID.randomUUID().toString() + extension;
            String filePath = uploadDir + IMAGE_DIR + newFilename;
            
            File dest = new File(filePath);
            if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
            file.transferTo(dest);

            String fileUrl = "/uploads/images/" + newFilename;
            Map<String, Object> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", newFilename);
            return ResponseVO.success(result);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseVO.error("文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/video")
    public ResponseVO<Map<String, Object>> uploadVideo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseVO.error("请选择要上传的文件");
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            return ResponseVO.error("请上传视频文件");
        }
        if (file.getSize() > 50 * 1024 * 1024) {
            return ResponseVO.error("视频大小不能超过50MB");
        }

        try {
            String extension = getExtension(file.getOriginalFilename(), ".mp4");
            String newFilename = UUID.randomUUID().toString() + extension;
            String filePath = uploadDir + VIDEO_DIR + newFilename;
            
            File dest = new File(filePath);
            if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
            file.transferTo(dest);

            String fileUrl = "/uploads/videos/" + newFilename;
            Map<String, Object> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", newFilename);
            return ResponseVO.success(result);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseVO.error("文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/audio")
    public ResponseVO<Map<String, Object>> uploadAudio(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseVO.error("请选择要上传的文件");
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            return ResponseVO.error("请上传音频文件");
        }
        if (file.getSize() > 20 * 1024 * 1024) {
            return ResponseVO.error("音频大小不能超过20MB");
        }

        try {
            String extension = getExtension(file.getOriginalFilename(), ".mp3");
            String newFilename = UUID.randomUUID().toString() + extension;
            String filePath = uploadDir + AUDIO_DIR + newFilename;
            
            File dest = new File(filePath);
            if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
            file.transferTo(dest);

            String fileUrl = "/uploads/audios/" + newFilename;
            Map<String, Object> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", newFilename);
            return ResponseVO.success(result);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseVO.error("文件上传失败: " + e.getMessage());
        }
    }

    private String getExtension(String filename, String defaultExt) {
        if (filename != null && filename.contains(".")) {
            String ext = filename.substring(filename.lastIndexOf(".")).toLowerCase();
            return ext;
        }
        return defaultExt;
    }

    /**
     * 获取默认头像（SVG格式）
     */
    @GetMapping(value = "/avatar/default", produces = "image/svg+xml")
    public @ResponseBody byte[] getDefaultAvatar() {
        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 100 100\">" +
                     "<circle cx=\"50\" cy=\"50\" r=\"45\" fill=\"#e0e0e0\" stroke=\"#bdbdbd\" stroke-width=\"2\"/>" +
                     "<circle cx=\"50\" cy=\"40\" r=\"18\" fill=\"#90caf9\"/>" +
                     "<circle cx=\"50\" cy=\"55\" r=\"12\" fill=\"#90caf9\"/>" +
                     "<circle cx=\"42\" cy=\"37\" r=\"3\" fill=\"#1a237e\"/>" +
                     "<circle cx=\"58\" cy=\"37\" r=\"3\" fill=\"#1a237e\"/>" +
                     "<path d=\"M 42 60 Q 50 68 58 60\" stroke=\"#1a237e\" stroke-width=\"2\" fill=\"none\"/>" +
                     "</svg>";
        return svg.getBytes(StandardCharsets.UTF_8);
    }
}

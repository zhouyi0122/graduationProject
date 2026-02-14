package com.campus.trade.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin
public class FileUploadController {

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择要上传的文件");
        }

        // 获取上传文件的原始名称和后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + suffix;

        // 确定保存路径（项目根目录下的 uploads 文件夹）
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
        File dest = new File(uploadPath, fileName);

        try {
            file.transferTo(dest);
            // 返回文件的访问 URL
            String fileUrl = "http://localhost:8080/api/uploads/" + fileName;
            return ResponseEntity.ok(Map.of("url", fileUrl));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("文件上传失败");
        }
    }
}

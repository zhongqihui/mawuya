/*
 * www.qihuizhong.com Inc.
 * Copyright (c) 2026 钟启辉. All Rights Reserved.
 */
package com.qihuizhong.mawuya.common.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 文件工具类
 *
 * @author zqh
 */
public class FileUtil {

    private FileUtil() {
    }

    /**
     * 上传文件到指定目录，文件名使用时间戳避免覆盖。
     *
     * @param file       上传文件
     * @param targetPath 目标目录（绝对路径）
     * @return 实际落地的文件名（不含目录）
     */
    public static String upload(MultipartFile file, String targetPath) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("file is empty");
        }
        String trueFileName = file.getOriginalFilename();
        if (trueFileName == null) {
            trueFileName = "unknown";
        }
        int dotIdx = trueFileName.lastIndexOf('.');
        String ext = dotIdx >= 0 ? trueFileName.substring(dotIdx) : "";
        String fileName = System.currentTimeMillis() + ext;

        File dir = new File(targetPath);
        if (!dir.exists()) {
            // mkdirs 才能递归创建父目录
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        File targetFile = new File(dir, fileName);
        file.transferTo(targetFile);
        return fileName;
    }
}

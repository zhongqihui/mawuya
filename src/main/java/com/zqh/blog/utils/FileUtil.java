package com.zqh.blog.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * author: zqh
 * email：zqhfsf@gmail.com
 * date: 2018/2/17 15:32
 * description:
 **/
public class FileUtil {

    public static String upload(MultipartFile file, String targetPath) throws Exception {
        String trueFileName = file.getOriginalFilename();
        String fileName = System.currentTimeMillis() + trueFileName.substring(trueFileName.lastIndexOf("."));
        File targetFile = new File(targetPath, fileName);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }

        file.transferTo(targetFile);
        return fileName;
    }
}

package com.iteaj.framework.spi.file;

import java.io.File;
import java.io.InputStream;

/**
 * 文件上传服务
 */
public interface UploadService {

    /**
     * 上传文件
     * @param stream
     * @param originalFilename 原文件名
     * @return 访问地址
     */
    UploadResult upload(InputStream stream, String originalFilename) throws FileException;

    /**
     * 上传文件
     * @param stream
     * @param originalFilename 原文件名
     * @param subPath 子路径
     * @return 访问地址
     */
    UploadResult upload(InputStream stream, String originalFilename, String subPath) throws FileException;

    /**
     * 上传文件
     * @param stream
     * @param originalFilename 原文件名
     * @param fileName 自定义文件名
     * @param subPath 子路径
     * @return 访问地址
     */
    UploadResult upload(InputStream stream, String originalFilename, String fileName, String subPath) throws FileException;

    /**
     * 返回文件类型
     * @param fileName
     * @return e.g .jpg
     */
    default String getFileType(String fileName) {
        return fileName.replaceFirst(".*\\.", ".");
    }
}

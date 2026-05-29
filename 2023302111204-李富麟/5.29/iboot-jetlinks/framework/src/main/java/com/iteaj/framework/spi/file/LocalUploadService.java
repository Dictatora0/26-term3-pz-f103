package com.iteaj.framework.spi.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.StrUtil;
import com.iteaj.framework.autoconfigure.FrameworkProperties;
import com.iteaj.framework.exception.ServiceException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class LocalUploadService implements UploadService, InitializingBean {

    private File rootDir; // 上传的跟目录
    private String uploadRootUri;
    private final FrameworkProperties properties;
    private String commonSubPath = File.separator + "file" + File.separator; // 默认的子路径

    public LocalUploadService(FrameworkProperties properties) {
        this.properties = properties;
    }

    @Override
    public UploadResult upload(InputStream stream, String originalFilename) throws FileException {
        return this.upload(stream, originalFilename, this.commonSubPath);
    }

    @Override
    public UploadResult upload(InputStream stream, String originalFilename, String subPath) throws FileException {
        return this.upload(stream, originalFilename, null, subPath);
    }

    @Override
    public UploadResult upload(InputStream stream, String originalFilename, String fileName, String subPath) throws FileException {
        try {
            fileName = StrUtil.isNotBlank(fileName) ? fileName :  UUID.randomUUID().toString();
            String filePath = genFilePath(originalFilename, fileName, subPath);

            File file = new File(rootDir, filePath);
            FileUtil.writeFromStream(stream, file);
            return new UploadResult(uploadRootUri + filePath, file, fileName, originalFilename);
        } catch (IORuntimeException e) {
            throw new FileException(e);
        }
    }

    /**
     * 生成文件路径, 使用指定的文件名
     * @param originalFilename 上传的文件名, 用来获取文件类型
     * @param fileName // 要创建的文件的文件名
     * @param subPath // 生成文件路径的子路径
     * @return
     */
    private String genFilePath(String originalFilename, String fileName, String subPath) {
        // 生成文件名
        String fileType = getFileType(originalFilename);
        // 文件名包含了文件后缀
        fileName = fileName.contains(fileType) ? fileName : fileName + fileType;

        // 生成文件路径
        if(!subPath.endsWith("/")) {
            subPath = subPath + "/";
        }

        return subPath + fileName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        FrameworkProperties.Upload upload = properties.getWeb().getUpload();
        String uploadRootDir = upload.getLocation();
        if(!StrUtil.isBlank(uploadRootDir)) {
            uploadRootDir = uploadRootDir.contains("file:") ?
                    uploadRootDir.split("file:")[1]
                    : uploadRootDir.split("classpath:")[1];

            if(uploadRootDir.endsWith(File.separator)) {
                uploadRootDir = uploadRootDir.substring(0, uploadRootDir.length()-1);
            }

            this.rootDir = new File(uploadRootDir);
            if(!this.rootDir.exists()) {
                this.rootDir.mkdirs();
            }

            if(!this.rootDir.isDirectory()) {
                throw new ServiceException("文件的上传根路径必须是目录："+uploadRootDir);
            }
        }
        String uploadRootUri = upload.getPattern();
        if(!StrUtil.isBlank(uploadRootUri)) {
            if(uploadRootUri.contains("*")) {
                uploadRootUri = uploadRootUri.replaceAll("\\*", "");
            }

            if(uploadRootUri.endsWith(File.separator)) {
                this.uploadRootUri = uploadRootUri.substring(0, uploadRootUri.length()-1);
            }

            this.uploadRootUri = uploadRootUri;
        } else {
            throw new ServiceException("未配置上传文件的访问pattern[framework.web.upload.pattern]");
        }
    }
}

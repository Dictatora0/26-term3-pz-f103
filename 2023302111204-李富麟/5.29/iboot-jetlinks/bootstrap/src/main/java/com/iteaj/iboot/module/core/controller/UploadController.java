package com.iteaj.iboot.module.core.controller;

import com.iteaj.framework.BaseController;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.logger.Logger;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.SecurityUtil;
import com.iteaj.framework.spi.file.UploadResult;
import com.iteaj.framework.spi.file.UploadService;
import com.iteaj.iboot.module.core.entity.Admin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 通用的文件上传
 * @author iteaj
 * @since 1.0
 */
@RestController
@RequestMapping("/common/upload")
public class UploadController extends BaseController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    /**
     * 文件上传
     * todo url
     * @param file
     * @return
     */
    @Logger("上传单文件")
    @PostMapping
    public Result<String> upload(MultipartFile file, String subPath) {
        try {
            UploadResult result = this.uploadService.upload(file.getInputStream(), file.getOriginalFilename(), subPath);
            return success(result.getUrl(), "上传成功");
        } catch (IOException e) {
            return fail("上传失败");
        }
    }

    /**
     * 文件批量上传
     * @param file
     * @return
     */
    @Logger("批量上传文件")
    @PostMapping("/batch")
    public Result<List<String>> batch(MultipartFile[] file, String subPath) {
        List<String> urls = new ArrayList<>();

        try {
            Arrays.asList(file).forEach(item -> {
                try {
                    UploadResult result = this.uploadService.upload(item.getInputStream(), item.getOriginalFilename(), subPath);
                    urls.add(result.getUrl());
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }

            });
        } catch (Exception e) {
            throw new ServiceException("上传文件失败", e);
        }

        return success(urls);
    }

    /**
     * 上传用户头像
     * @param file
     * @return
     */
    @PostMapping("avatar")
    public Result avatar(MultipartFile file) {
        try {
            Admin admin = SecurityUtil.getLoginUser().map(item -> (Admin) item).get();
            UploadResult result = this.uploadService.upload(file.getInputStream()
                    , file.getOriginalFilename(), "avatar-" + admin.getAccount(), "/avatar");
            return success(result.getUrl(), "上传成功");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return fail("上传失败");
        }
    }

}

package com.iteaj.framework.autoconfigure;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.iteaj.framework.captcha.CaptchaMapper;
import com.iteaj.framework.captcha.CaptchaService;
import com.iteaj.framework.captcha.KaptchaCreator;
import com.iteaj.framework.consts.CoreConst;
import com.iteaj.framework.exception.ServiceException;
import com.iteaj.framework.result.HttpResult;
import com.iteaj.framework.result.Result;
import com.iteaj.framework.security.OrderFilterChainDefinition;
import com.iteaj.framework.security.SecurityUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 校验码功能
 * @author iteaj
 * @since 1.0
 */
@EnableScheduling
@RequestMapping("/valid")
@Controller("CaptchaController")
@MapperScan(basePackages = "com.iteaj.framework.captcha")
public class CaptchaAutoConfiguration {

    @Autowired
    private CaptchaService captchaService;

    /**
     * 生成code
     * @return
     */
    @ResponseBody
    @GetMapping("/code")
    public Result<String> code() {
        String uuid = IdUtil.getSnowflakeNextIdStr();
        return HttpResult.Success(uuid);
    }

    /**
     * 获取验证码
     * @return
     */
    @GetMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter(CoreConst.CAPTCHA_CODE);
        if(StrUtil.isBlank(code)) {
            throw new ServiceException("未指定code");
        }

        try(ServletOutputStream out = response.getOutputStream()){
            handleResponseHeader(response);
            BufferedImage image = captchaService.createCaptcha(code);

            // 写入图片到输出流
            ImageIO.write(image, "jpg", out);
            out.flush();
        }
    }

    private void handleResponseHeader(HttpServletResponse response) {
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
    }

    @Bean
    @Order(20)
    public OrderFilterChainDefinition captchaFilterChainDefinition() {
        return new OrderFilterChainDefinition().addAnon("/valid/**");
    }

    /**
     * 字符格式的验证码
     * @return
     */
    @Bean(name = "charCaptcha")
    @ConditionalOnProperty(value = "framework.captcha.type", havingValue = "Char")
    public Producer charCaptcha(){
        return KaptchaCreator.charInstance();
    }

    /**
     * 数学计算加、减、乘格式的验证码
     * @return
     */
    @Primary
    @Bean(name = "mathCaptcha")
    @ConditionalOnProperty(value = "framework.captcha.type", havingValue = "Math", matchIfMissing = true)
    public Producer mathCaptcha(){
        return KaptchaCreator.mathInstance();
    }

    /**
     * 验证码服务
     * @param captchaMapper
     * @param producer
     * @return
     */
    @Bean
    public CaptchaService captchaService(CaptchaMapper captchaMapper
            , Producer producer, FrameworkProperties properties) {
        return new CaptchaService(producer, captchaMapper, properties);
    }
}

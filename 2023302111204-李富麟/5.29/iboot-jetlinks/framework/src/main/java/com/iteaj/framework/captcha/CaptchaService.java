package com.iteaj.framework.captcha;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.code.kaptcha.Producer;
import com.iteaj.framework.autoconfigure.FrameworkProperties;

import java.awt.image.BufferedImage;

public class CaptchaService {

    private final Producer producer;
    private final CaptchaMapper captchaMapper;
    private final FrameworkProperties properties;

    public CaptchaService(Producer producer, CaptchaMapper captchaMapper, FrameworkProperties properties) {
        this.producer = producer;
        this.captchaMapper = captchaMapper;
        this.properties = properties;
    }

    /**
     * 创建校验码
     * @param code
     * @return
     */
    public BufferedImage createCaptcha(String code) {
        // 创建要生产的文本已经图片
        // MathTextCreator对象创建的文本用'@'分割文本和值 e.g. 3+5=?@8
        String[] split = producer.createText().split("@");
        BufferedImage image = producer.createImage(split[0]);

        // 将校验码放入缓存
        long expire = properties.getCaptcha().getExpire();
        Captcha captcha = new Captcha(code, split[1], expire);
        try {
            captchaMapper.insert(captcha);
        } catch (Exception e) { }

        return image;
    }

    /**
     * 获取指定的检验码
     * @param code
     * @return 校验码对象
     */
    public Captcha getCaptcha(String code) {
        return captchaMapper.selectOne(Wrappers.<Captcha>lambdaQuery().eq(Captcha::getCode, code));
    }

    /**
     * 移除校验码
     * @param code
     * @return
     */
    public Captcha removeCaptcha(String code) {
        Captcha captcha = this.getCaptcha(code);
        if(captcha != null) {
            captchaMapper.delete(Wrappers.<Captcha>lambdaQuery().eq(Captcha::getCode, code));
        }
        return captcha;
    }
}

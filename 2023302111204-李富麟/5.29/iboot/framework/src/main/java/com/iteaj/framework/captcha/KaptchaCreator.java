package com.iteaj.framework.captcha;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

import java.util.Properties;
/**
 * kaptcha.border 是否有边框默认为true 我们可以自己设置yes，no
 * kaptcha.border.color 边框颜色默认为Color.BLACK
 * kaptcha.border.thickness 边框粗细度默认为1
 * kaptcha.producer.impl 验证码生成器默认为DefaultKaptcha
 * kaptcha.textproducer.impl 验证码文本生成器默DefaultTextCreator
 * kaptcha.textproducer.char.string 验证码文本字符内容范围默认为abcde2345678gfynmnpwx
 * kaptcha.textproducer.char.length 验证码文本字符长度默认为5
 * kaptcha.textproducer.font.names 验证码文本字体样式默认为new Font("Arial", 1, fontSize), new Font("Courier", 1, fontSize)
 * kaptcha.textproducer.font.size 验证码文本字符大小默认为40
 * kaptcha.textproducer.font.color 验证码文本字符颜色默认为Color.BLACK
 * kaptcha.textproducer.char.space 验证码文本字符间距默认为2
 * kaptcha.noise.impl 验证码噪点生成对象默认为DefaultNoise
 * kaptcha.noise.color 验证码噪点颜色默认为Color.BLACK
 * kaptcha.obscurificator.impl 验证码样式引擎默认为WaterRipple
 * kaptcha.word.impl 验证码文本字符渲染默认为DefaultWordRenderer
 * kaptcha.background.impl 验证码背景生成器默认为DefaultBackground
 * kaptcha.background.clear.from 验证码背景颜色渐进默认为Color.LIGHT_GRAY
 * kaptcha.background.clear.to 验证码背景颜色渐进默认为Color.WHITE
 * kaptcha.image.width 验证码图片宽度默认为200
 * kaptcha.image.height 验证码图片高度默认为50
 */
public class KaptchaCreator {

    public static Producer charInstance() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = getKaptchaProperties();
        properties.setProperty(Constants.KAPTCHA_NOISE_COLOR, "white");
        properties.setProperty(Constants.KAPTCHA_SESSION_KEY, "kaptchaCode");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, "28");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_SPACE, "35");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "5");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_NAMES, "Arial,Courier");

        defaultKaptcha.setConfig(new Config(properties));
        return defaultKaptcha;
    }

    public static Producer mathInstance() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = getKaptchaProperties();
        properties.setProperty(Constants.KAPTCHA_NOISE_COLOR, "white");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, "38");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_SPACE, "5");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "6");
        properties.setProperty(Constants.KAPTCHA_SESSION_KEY, "kaptchaCodeMath");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_NAMES, "Arial,Courier");
        properties.setProperty(Constants.KAPTCHA_NOISE_IMPL, "com.google.code.kaptcha.impl.NoNoise");
        properties.setProperty(Constants.KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.ShadowGimpy");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_IMPL, "com.iteaj.framework.captcha.MathTextCreator");

        defaultKaptcha.setConfig(new Config(properties));
        return defaultKaptcha;
    }

    private static Properties getKaptchaProperties() {
        Properties properties = new Properties();
        properties.setProperty(Constants.KAPTCHA_BORDER, "no");
        properties.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, "160");
        properties.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, "50");
        properties.setProperty(Constants.KAPTCHA_BORDER_COLOR, "105,179,90");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_COLOR, "blue");
        return properties;
    }
}

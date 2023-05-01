package com.lsy.sunny.web.controller.common;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import com.lsy.sunny.common.config.SunnyConfig;
import com.lsy.sunny.common.constant.Constants;
import com.lsy.sunny.common.utils.sign.Base64;
import com.lsy.sunny.common.utils.uuid.IdUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.code.kaptcha.Producer;
import com.lsy.sunny.common.core.domain.AjaxResult;
import com.lsy.sunny.common.core.redis.RedisCache;
import com.lsy.sunny.system.service.ISysConfigService;

/**
 * 验证码操作处理
 *
 * @author sunny
 */
@RestController
public class CaptchaController {
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysConfigService configService;

    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public AjaxResult getCode(HttpServletResponse response) throws IOException {
        AjaxResult ajax = AjaxResult.success();
        //去数据库中查询验证码是否开启，在数据库的sys_config表中有一个关于验证码的信息。
        boolean captchaOnOff = configService.selectCaptchaOnOff();
        ajax.put("captchaOnOff", captchaOnOff);
        if (!captchaOnOff) {
            return ajax;
        }

        // 保存验证码信息
        String uuid = IdUtils.simpleUUID();
        String verifyKey = Constants.CAPTCHA_CODE_KEY + uuid;

        String capStr = null, code = null;
        BufferedImage image = null;

        // 生成验证码
        String captchaType = SunnyConfig.getCaptchaType();
        if ("math".equals(captchaType)) {
            String capText = captchaProducerMath.createText();
            //将生成验证码的文本进行打印
            //格式：1+1=？@2   8+2=?@10
            System.out.println(capText);
            //计算的文本
            capStr = capText.substring(0, capText.lastIndexOf("@"));
           // 结果文本
            code = capText.substring(capText.lastIndexOf("@") + 1);
            //根据上面的文本生成图像
            image = captchaProducerMath.createImage(capStr);
        } else if ("char".equals(captchaType)) {
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        }

        //过期时间两分钟
        redisCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
        } catch (IOException e) {
            return AjaxResult.error(e.getMessage());
        }

        ajax.put("uuid", uuid);
        //base64转码，返回到前端，ajax是返回到前端的封装格式。
        ajax.put("img", Base64.encode(os.toByteArray()));
        return ajax;
    }
}

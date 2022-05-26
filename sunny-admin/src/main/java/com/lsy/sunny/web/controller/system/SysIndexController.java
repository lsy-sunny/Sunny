package com.lsy.sunny.web.controller.system;

import com.lsy.sunny.common.config.SunnyConfig;
import com.lsy.sunny.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页
 *
 * @author sunny
 */
@RestController
public class SysIndexController {
    /**
     * 系统基础配置
     */
    @Autowired
    private SunnyConfig sunnyConfig;

    /**
     * 访问首页，提示语
     */
    @RequestMapping("/")
    public String index() {
        return StringUtils.format("欢迎使用{}后台管理框架，当前版本：v{}，请通过前端地址访问。", sunnyConfig.getName(), sunnyConfig.getVersion());
    }
}

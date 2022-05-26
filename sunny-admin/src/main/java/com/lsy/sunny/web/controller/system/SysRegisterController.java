package com.lsy.sunny.web.controller.system;

import com.lsy.sunny.common.core.controller.BaseController;
import com.lsy.sunny.common.core.domain.model.RegisterBody;
import com.lsy.sunny.common.utils.StringUtils;
import com.lsy.sunny.framework.web.service.SysRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.lsy.sunny.common.core.domain.AjaxResult;
import com.lsy.sunny.system.service.ISysConfigService;

/**
 * 注册验证
 *
 * @author sunny
 */
@RestController
public class SysRegisterController extends BaseController {
    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private ISysConfigService configService;

    @PostMapping("/register")
    public AjaxResult register(@RequestBody RegisterBody user) {
        if (!("true".equals(configService.selectConfigByKey("sys.account.registerUser")))) {
            return error("当前系统没有开启注册功能！");
        }
        String msg = registerService.register(user);
        return StringUtils.isEmpty(msg) ? success() : error(msg);
    }
}

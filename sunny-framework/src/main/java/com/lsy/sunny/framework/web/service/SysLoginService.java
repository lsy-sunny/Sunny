package com.lsy.sunny.framework.web.service;

import javax.annotation.Resource;

import com.lsy.sunny.common.constant.Constants;
import com.lsy.sunny.common.core.domain.model.LoginUser;
import com.lsy.sunny.common.exception.ServiceException;
import com.lsy.sunny.common.utils.DateUtils;
import com.lsy.sunny.common.utils.MessageUtils;
import com.lsy.sunny.common.utils.ServletUtils;
import com.lsy.sunny.common.utils.StringUtils;
import com.lsy.sunny.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.lsy.sunny.common.core.domain.entity.SysUser;
import com.lsy.sunny.common.core.redis.RedisCache;
import com.lsy.sunny.common.exception.user.CaptchaException;
import com.lsy.sunny.common.exception.user.CaptchaExpireException;
import com.lsy.sunny.common.exception.user.UserPasswordNotMatchException;
import com.lsy.sunny.common.utils.ip.IpUtils;
import com.lsy.sunny.framework.manager.AsyncManager;
import com.lsy.sunny.framework.manager.factory.AsyncFactory;
import com.lsy.sunny.system.service.ISysConfigService;

/**
 * 登录校验方法
 *
 * @author sunny
 */
@Component
public class SysLoginService {
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid) {
        boolean captchaOnOff = configService.selectCaptchaOnOff();
        // 验证码开关
        if (captchaOnOff) {
            validateCaptcha(username, code, uuid);
        }
        // 用户验证
        Authentication authentication = null;
        try {
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            //这句话的意思就是在执行登录
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            } else {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser.getUserId());
        // 生成token
        return tokenService.createToken(loginUser);
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid) {

        String verifyKey = Constants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        //获取到redis中保存的验证码文本
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        //如果redis中不存在这个验证码，就登陆失败了，（过期时间到了之后，redis中不会再有这个验证码的记录）
        if (captcha == null) {
            // 开启一个异步任务去写日志，将其记录在日志表中，就是数据库中的表
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
            throw new CaptchaExpireException();
        }
        //如果验证码与redis中的不一样
        if (!code.equalsIgnoreCase(captcha)) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
        //只要不出现异常就表示验证通过了。
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
        sysUser.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(sysUser);
    }
}

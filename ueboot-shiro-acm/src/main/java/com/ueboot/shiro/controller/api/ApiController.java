package com.ueboot.shiro.controller.api;


import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.ueboot.core.exception.BusinessException;
import com.ueboot.core.http.response.Response;
import com.ueboot.core.utils.CaptchaUtils;
import com.ueboot.shiro.entity.Resources;
import com.ueboot.shiro.entity.User;
import com.ueboot.shiro.service.resources.ResourcesService;
import com.ueboot.shiro.service.user.UserService;
import com.ueboot.shiro.shiro.ShiroEventListener;
import com.ueboot.shiro.shiro.ShiroService;
import com.ueboot.shiro.shiro.UserRealm;
import com.ueboot.shiro.shiro.cache.RedisCache;
import com.ueboot.shiro.shiro.handler.ShiroExceptionHandler;
import com.ueboot.shiro.shiro.processor.ShiroProcessor;
import com.ueboot.shiro.util.PasswordUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import jodd.datetime.JDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 *
 * ueboot-shiro 对外提供Api接口（用户登录、退出、验证码、用户菜单、）
 *
 * @author yangkui
 */
@Slf4j
@RestController
@RequestMapping(value = "/ueboot/shiro")
public class ApiController {

    private static final String CAPTCHA_KEY = "UEBOOT_SHIRO_CAPTCHA_CODE";

    private final ShiroProcessor shiroProcessor;

    private final ResourcesService resourcesService;

    private final UserService userService;

    private final ShiroService shiroService;

    private final ShiroEventListener shiroEventListener;

    private final UserRealm userRealm;


    @Autowired
    public ApiController(ShiroProcessor shiroProcessor, ResourcesService resourcesService,
                         UserService userService, ShiroService shiroService, ShiroEventListener shiroEventListener,UserRealm userRealm) {
        this.shiroProcessor = shiroProcessor;
        this.resourcesService = resourcesService;
        this.userService = userService;
        this.shiroService = shiroService;
        this.shiroEventListener = shiroEventListener;
        this.userRealm = userRealm;
    }

    @PostMapping(value = "/public/login")
    @ApiOperation(value = "用户登录",notes = "校验码的获取参加校验码获取接口，返回接口后台可以自定义")
    @ApiImplicitParam(name = "params", value = "登录接口参数", required = true, dataType = "LoginVo")
    public Response<Map<String, Object>> login(@RequestBody LoginVo params, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String sessionCaptcha = (String) session.getAttribute(CAPTCHA_KEY);
        log.debug("从session当中获取的验证码:{},用户提交的验证码:{}", sessionCaptcha, params.getCaptcha());
        if (sessionCaptcha != null && params.getCaptcha().toLowerCase().equals(sessionCaptcha.toLowerCase())) {
            session.setAttribute(CAPTCHA_KEY, "");
        } else {
            session.setAttribute(CAPTCHA_KEY, "");
            throw new BusinessException("验证码不正确!");
        }
        String loginMessage="";
        shiroEventListener.beforeLogin(params.getUsername(), params.getCaptcha());
        ShiroExceptionHandler.set(params.getUsername());
        this.shiroProcessor.login(params.getUsername(), params.getPassword());
        if (!StringUtils.isEmpty(params.getUsername())) {
            shiroEventListener.afterLogin(params.getUsername(), true, loginMessage);
        }
        ShiroExceptionHandler.remove();
        //返回登录成功后的信息
        Map<String, Object> info = this.shiroService.getLoginSuccessInfo(params.getUsername());
        return new Response<>(info);
    }

    @PostMapping(value = "/private/logout")
    @ApiOperation(value = "用户退出")
    public Response<Void> logout() {
        // 登出日志记录
        String currentUserName = (String) SecurityUtils.getSubject().getPrincipal();
        this.shiroEventListener.loginOutEvent(currentUserName);
        this.shiroProcessor.logout();
        return new Response<>();
    }


    @RequiresAuthentication
    @RequestMapping(value = "/private/updatePassword")
    @ApiOperation(value = "更新密码")
    public Response<Void> updatePassword(@RequestBody UpdatePasswordReq req) {
        String userName = (String) SecurityUtils.getSubject().getPrincipal();
        //加密旧密码
        String oldPassword = PasswordUtil.sha512(userName, req.getOldPassword().toLowerCase());
        //加密新密码
        String newPassword = PasswordUtil.sha512(userName, req.getNewPassword().toLowerCase());
        User user = userService.findByUserName(userName);
        if (!user.getPassword().equals(oldPassword)) {
            throw new BusinessException("原密码输入错误,请重新输入");
        }
        user.setPassword(newPassword);
        JDateTime dateTime = new JDateTime();
        //默认密码过期日期为x个月，x个月后要求更换密码
        Date expiredDate = dateTime.addMonth(this.shiroService.getPasswordExpiredMonth()).convertToDate();
        user.setCredentialExpiredDate(expiredDate);
        this.userService.save(user);

        // 更新密码日志记录
        this.shiroEventListener.updatePasswordEvent(userName);
        return new Response<Void>();
    }


    /**
     * 获取登录用户的菜单资源
     *
     * @return 菜单资源
     */
    @RequiresAuthentication
    @RequestMapping(value = "/private/menus", method = RequestMethod.GET)
    @ApiOperation(value = "获取登录用户的菜单资源")
    public Response<List<MenuVo>> menus() {
        Subject currentUser = SecurityUtils.getSubject();
        String username = (String) currentUser.getPrincipal();

        Collection<Resources> resources = this.resourcesService.getUserResources(username);
        //查询出所有菜单组资源。防止授权时未勾选菜单组，导致前端页面没有菜单出现
        List<Resources> groups = this.resourcesService.findByResourceType(Resources.RESOURCE_TYPE_GROUP);
        List<MenuVo> body = new ArrayList<>();
        List<Resources> parents = new ArrayList<>();
        Map<Long, Resources> resourcesMap = new HashMap<>();
        for (Resources resource : resources) {
            if (Resources.RESOURCE_TYPE_BUTTON.equals(resource.getResourceType())) {
                continue;
            }
            if (resource.getParent() != null) {
                parents.add(resource.getParent());
            }
            resourcesMap.put(resource.getId(), resource);
            body.add(assembleMenuVo(resource));
        }
        //查找所有父节点是否在结果集当中，不在则需要获取
        parents.forEach((p) -> {
            Resources parent = resourcesMap.get(p.getId());
            if (parent == null) {
                Iterator<Resources> it = groups.iterator();
                while (it.hasNext()) {
                    Resources g = it.next();
                    if (p.getId().equals(g.getId())) {
                        body.add(assembleMenuVo(g));
                        groups.remove(g);
                    }
                }
            }
        });
        return new Response<>(body);
    }

    private MenuVo assembleMenuVo(Resources resource) {
        MenuVo menu = new MenuVo();
        BeanUtils.copyProperties(resource, menu);
        if (resource.getParent() != null) {
            menu.setParentId(resource.getParent().getId());
        }
        menu.setThemeJson(StringUtils.isEmpty(resource.getThemeJson()) ? new HashMap() : JSON.parseObject(resource.getThemeJson(), Map.class));
        return menu;
    }

    /**
     * 获取验证码
     *
     * @param request  request
     * @param response response
     * @throws IOException IOException
     */
    @RequestMapping(value = "/public/captcha", method = RequestMethod.GET)
    @ApiOperation(value = "获取验证码")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0L);
        response.setContentType("image/jpeg");
        String codeCountStr = request.getParameter("codeCount");
        String widthStr = request.getParameter("width");
        String heightStr = request.getParameter("height");
        int codeCount = 4;
        int w = 200;
        int h = 80;
        if(StrUtil.isNotBlank(codeCountStr)){
            codeCount = Integer.parseInt(codeCountStr);
        }
        if(StrUtil.isNotBlank(widthStr)){
            w = Integer.parseInt(widthStr);
        }
        if(StrUtil.isNotBlank(heightStr)){
            h = Integer.parseInt(heightStr);
        }

        LineCaptcha captcha = CaptchaUtils.getLineCaptcha(w,h,codeCount);
        try {
            HttpSession session = request.getSession();
            session.setAttribute(CAPTCHA_KEY, captcha.getCode().toLowerCase());
            ImageIO.write(captcha.getImage(), "jpg", response.getOutputStream());
        } catch (IOException e) {
            log.error(e.getMessage(),e);
            throw new BusinessException("验证码生成异常");
        }
    }
}

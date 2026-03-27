package com.seckill.controller;

import com.seckill.dto.LoginFormDTO;
import com.seckill.dto.Result;
import com.seckill.dto.UserDTO;
import com.seckill.service.IUserService;
import com.seckill.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: UserController
 * Package: com.seckill.controller
 * Description:
 *
 * @Author AnXin
 * @Create 2026/3/27 16:42
 * @Version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    /**
     * 发送手机验证码
     */
    @PostMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone) {
        // 发送短信验证码并保存验证码
        return userService.sendCode(phone);
    }

    /**
     * 登录功能
     * @param loginFormDTO 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginFormDTO) {
        // 实现登录功能
        return userService.login(loginFormDTO);
    }

    /**
     * 获取当前登录用户信息
     */
    public Result me() {
        // 获取当前登录的用户并返回
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }
}

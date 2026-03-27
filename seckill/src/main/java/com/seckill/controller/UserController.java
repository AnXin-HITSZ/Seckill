package com.seckill.controller;

import com.seckill.dto.LoginFormDTO;
import com.seckill.dto.Result;
import com.seckill.dto.UserDTO;
import com.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

    /**
     * 发送手机验证码
     */
    @PostMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // 发送短信验证码并保存验证码
        return userService.sendCode(phone, session);
    }

    /**
     * 登录功能
     * @param loginFormDTO 登录参数，包含手机号、验证码；或者手机号、密码
     * @param session
     * @return
     */
    public Result login(@RequestBody LoginFormDTO loginFormDTO, HttpSession session) {
        // 实现登录功能
        return userService.login(loginFormDTO, session);
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

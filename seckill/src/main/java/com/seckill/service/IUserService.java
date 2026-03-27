package com.seckill.service;

import com.seckill.dto.LoginFormDTO;
import com.seckill.dto.Result;
import jakarta.servlet.http.HttpSession;

/**
 * ClassName: IUserService
 * Package: com.seckill.service
 * Description:
 *
 * @Author AnXin
 * @Create 2026/3/27 16:57
 * @Version 1.0
 */
public interface IUserService {
    Result sendCode(String phone);

    Result login(LoginFormDTO loginFormDTO);
}

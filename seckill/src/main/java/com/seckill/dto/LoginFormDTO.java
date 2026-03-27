package com.seckill.dto;

import lombok.Data;

/**
 * ClassName: LoginFormDTO
 * Package: com.seckill.dto
 * Description:
 *
 * @Author AnXin
 * @Create 2026/3/27 16:47
 * @Version 1.0
 */
@Data
public class LoginFormDTO {
    private String phone;
    private String code;
    private String password;
}

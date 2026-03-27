package com.seckill.utils;

import com.seckill.dto.UserDTO;

/**
 * ClassName: UserHolder
 * Package: com.utils
 * Description:
 *
 * @Author AnXin
 * @Create 2026/3/27 16:54
 * @Version 1.0
 */
public class UserHolder {

    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user) {
        tl.set(user);
    }

    public static UserDTO getUser() {
        return tl.get();
    }

    public static void removeUser() {
        tl.remove();
    }
}

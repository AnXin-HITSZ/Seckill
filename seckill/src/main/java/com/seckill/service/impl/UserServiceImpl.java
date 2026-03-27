package com.seckill.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.dto.LoginFormDTO;
import com.seckill.dto.Result;
import com.seckill.dto.UserDTO;
import com.seckill.entity.User;
import com.seckill.mapper.UserMapper;
import com.seckill.service.IUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.seckill.utils.RedisConstants.*;
import static com.seckill.utils.SystemConstants.USER_NICKNAME_PREFIX;

/**
 * ClassName: UserServiceImpl
 * Package: com.seckill.service.impl
 * Description:
 *
 * @Author AnXin
 * @Create 2026/3/27 16:58
 * @Version 1.0
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone) {
        // 1. 生成验证码
        String code = RandomUtil.randomNumbers(6);
        // 2. 保存验证码到 Redis  // set key value ex 120
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 3. 发送验证码
        log.debug("发送短信验证码成功，验证码：{}", code);
        // 4. 返回 ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginFormDTO) {
        // 1. 从前端获取手机号及验证码
        String phone = loginFormDTO.getPhone();
        String code = loginFormDTO.getCode();
        // 2. 从 Redis 获取验证码并校验
        String redisCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        if (redisCode == null || !redisCode.equals(code)) {
            log.debug("验证码错误，错误的验证码：{}", code);
            return Result.fail("验证码错误");
        }
        User user = query().eq("phone", phone).one();
        if (user == null) {
            log.debug("该用户不存在，创建新用户");
            user = createUserWithPhone(phone);
        }
        // 3. 保存用户信息到 Redis 中
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(
                userDTO,
                new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString())
        );
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 4. 返回 token
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        // 1. 创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICKNAME_PREFIX + RandomUtil.randomString(10));
        // 2. 保存用户
        save(user);
        return user;
    }
}

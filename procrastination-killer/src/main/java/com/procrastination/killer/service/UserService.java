package com.procrastination.killer.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.procrastination.killer.common.JwtUtil;
import com.procrastination.killer.dto.LoginRequest;
import com.procrastination.killer.dto.LoginResponse;
import com.procrastination.killer.dto.RegisterRequest;
import com.procrastination.killer.entity.User;
import com.procrastination.killer.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务
 * 处理注册、登录核心业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     *
     * @param request 注册请求（用户名、密码、邮箱、昵称）
     * @return 注册成功的用户信息（不含密码）
     */
    public User register(RegisterRequest request) {
        // 1. 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new RuntimeException("用户名已被注册");
        }

        // 2. 检查邮箱是否已存在
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getEmail, request.getEmail()));
            if (count > 0) {
                throw new RuntimeException("邮箱已被注册");
            }
        }

        // 3. 构建用户实体，密码使用 BCrypt 加密
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .enabled(1)
                .build();

        // 4. 保存到数据库
        userMapper.insert(user);
        log.info("用户注册成功: username={}, userId={}", user.getUsername(), user.getId());

        // 5. 返回用户信息（清除密码字段）
        user.setPassword(null);
        return user;
    }

    /**
     * 用户登录
     *
     * @param request 登录请求（用户名、密码）
     * @return 登录响应（JWT Token + 用户信息）
     */
    public LoginResponse login(LoginRequest request) {
        // 1. 根据用户名查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 检查账号是否启用
        if (user.getEnabled() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 3. 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 4. 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("用户登录成功: username={}, userId={}", user.getUsername(), user.getId());

        // 5. 构建响应
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }
}

package com.procrastination.killer.controller;

import com.procrastination.killer.common.Result;
import com.procrastination.killer.dto.LoginRequest;
import com.procrastination.killer.dto.LoginResponse;
import com.procrastination.killer.dto.RegisterRequest;
import com.procrastination.killer.entity.User;
import com.procrastination.killer.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户认证接口
 */
@Tag(name = "用户认证", description = "注册、登录相关接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册", description = "创建新账号，密码使用BCrypt加密存储")
    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return Result.success("注册成功", user);
    }

    @Operation(summary = "用户登录", description = "验证用户名密码，返回JWT Token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }
}

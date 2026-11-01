package com.example.back_end.controller;

import com.example.back_end.common.Result;
import com.example.back_end.dto.request.ChangePasswordRequest;
import com.example.back_end.dto.request.UpdateUserRequest;
import com.example.back_end.dto.response.UserInfoResponse;
import com.example.back_end.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/info")
    public Result<UserInfoResponse> getInfo() {
        return Result.ok(userService.getInfo());
    }

    @PutMapping("/info")
    public Result<UserInfoResponse> updateInfo(@Valid @RequestBody UpdateUserRequest req) {
        return Result.ok(userService.updateInfo(req));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(req);
        return Result.ok("密码修改成功", null);
    }

    @DeleteMapping("/account")
    public Result<Void> deleteAccount() {
        userService.deleteAccount();
        return Result.ok("账号已永久注销", null);
    }
}

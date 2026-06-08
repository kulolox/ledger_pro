package com.example.back_end.controller;

import com.example.back_end.dto.request.UpdateUserRequest;
import com.example.back_end.dto.response.UserInfoResponse;
import com.example.back_end.service.UserService;
import com.example.back_end.utils.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/info")
    public Result<UserInfoResponse> getInfo() {
        return Result.ok(userService.getInfo());
    }

    @PutMapping("/info")
    public Result<UserInfoResponse> updateInfo(@RequestBody UpdateUserRequest req) {
        return Result.ok(userService.updateInfo(req));
    }
}

package com.example.back_end.controller;

import com.example.back_end.dto.request.UpdateUserRequest;
import com.example.back_end.dto.response.UserInfoResponse;
import com.example.back_end.service.IUserService;
import com.example.back_end.utils.Result;
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
    public Result<UserInfoResponse> updateInfo(@RequestBody UpdateUserRequest req) {
        return Result.ok(userService.updateInfo(req));
    }
}

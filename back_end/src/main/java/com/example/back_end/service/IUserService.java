package com.example.back_end.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.request.UpdateUserRequest;
import com.example.back_end.dto.response.LoginResponse;
import com.example.back_end.dto.response.UserInfoResponse;
import com.example.back_end.entity.User;

public interface IUserService extends IService<User> {
    LoginResponse register(RegisterRequest req);
    LoginResponse login(String email, String rawPassword);
    UserInfoResponse getInfo();
    UserInfoResponse updateInfo(UpdateUserRequest req);
}

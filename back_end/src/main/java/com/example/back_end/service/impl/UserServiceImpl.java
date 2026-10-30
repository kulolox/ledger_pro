package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.common.BizException;
import com.example.back_end.dto.request.UpdateUserRequest;
import com.example.back_end.dto.response.UserInfoResponse;
import com.example.back_end.entity.User;
import com.example.back_end.mapper.UserMapper;
import com.example.back_end.service.IUserService;
import com.example.back_end.utils.AuthUtils;
import com.example.back_end.utils.SanitizeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    public UserInfoResponse getInfo() {
        Long userId = AuthUtils.getCurrentUserId();
        User user = getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return toUserInfo(user);
    }

    public UserInfoResponse updateInfo(UpdateUserRequest req) {
        Long userId = AuthUtils.getCurrentUserId();
        User user = getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        if (req.nickname() != null) user.setNickname(SanitizeUtils.escapeHtml(req.nickname()));
        if (req.avatar() != null) user.setAvatarUrl(SanitizeUtils.escapeHtml(req.avatar()));
        if (req.phone() != null) user.setPhone(req.phone());

        updateById(user);
        log.info("更新用户信息 userId={}", userId);
        return toUserInfo(user);
    }

    private UserInfoResponse toUserInfo(User user) {
        return new UserInfoResponse(
                user.getId(), user.getNickname(), user.getAvatarUrl(), user.getEmail(), user.getPhone());
    }
}

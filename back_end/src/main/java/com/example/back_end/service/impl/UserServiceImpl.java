package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.request.UpdateUserRequest;
import com.example.back_end.dto.response.LoginResponse;
import com.example.back_end.dto.response.UserInfoResponse;
import com.example.back_end.entity.Account;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.User;
import com.example.back_end.exception.BizException;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.AccountMapper;
import com.example.back_end.mapper.UserMapper;
import com.example.back_end.service.IUserService;
import com.example.back_end.utils.AuthUtils;
import com.example.back_end.utils.JwtUtils;
import com.example.back_end.utils.SanitizeUtils;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private final AccountBookMapper accountBookMapper;
    private final AccountMapper accountMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserServiceImpl(
            AccountBookMapper accountBookMapper,
            AccountMapper accountMapper,
            BCryptPasswordEncoder passwordEncoder,
            JwtUtils jwtUtils) {
        this.accountBookMapper = accountBookMapper;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (lambdaQuery().eq(User::getEmail, req.email()).count() > 0) {
            throw new BizException("邮箱已注册");
        }

        User user = new User();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setNickname(req.nickname() != null ? SanitizeUtils.escapeHtml(req.nickname()) : "");
        save(user);

        AccountBook book = new AccountBook();
        book.setId(generateBookId());
        book.setName("日常账本");
        book.setOwnerId(user.getId());
        book.setType(0);
        accountBookMapper.insert(book);

        Account account = new Account();
        account.setBookId(book.getId());
        account.setName("现金");
        account.setType(0);
        account.setIcon("💵");
        accountMapper.insert(account);

        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getEmail());
        return new LoginResponse(token, refreshToken, toUserInfo(user));
    }

    private Long generateBookId() {
        long id;
        do {
            id = ThreadLocalRandom.current().nextLong(10000000L, 100000000000L);
        } while (accountBookMapper.selectById(id) != null);
        return id;
    }

    public LoginResponse login(String email, String rawPassword) {
        User user = lambdaQuery().eq(User::getEmail, email).one();
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BizException(401, "邮箱或密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getEmail());
        return new LoginResponse(token, refreshToken, toUserInfo(user));
    }

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
        user.setUpdatedAt(LocalDateTime.now());

        updateById(user);
        return toUserInfo(user);
    }

    private UserInfoResponse toUserInfo(User user) {
        return new UserInfoResponse(
                user.getId(), user.getNickname(), user.getAvatarUrl(), user.getEmail(), user.getPhone());
    }
}

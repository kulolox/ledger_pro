package com.example.back_end.service;

import com.example.back_end.dto.request.RegisterRequest;
import com.example.back_end.dto.request.UpdateUserRequest;
import com.example.back_end.dto.response.LoginResponse;
import com.example.back_end.dto.response.UserInfoResponse;
import com.example.back_end.entity.Account;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.User;
import com.example.back_end.exception.BizException;
import com.example.back_end.repository.AccountBookRepository;
import com.example.back_end.repository.AccountRepository;
import com.example.back_end.repository.UserRepository;
import com.example.back_end.utils.AuthUtils;
import com.example.back_end.utils.JwtUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AccountBookRepository accountBookRepository;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public UserService(UserRepository userRepository,
                       AccountBookRepository accountBookRepository,
                       AccountRepository accountRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.accountBookRepository = accountBookRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BizException("邮箱已注册");
        }

        User user = new User();
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setNickname(req.nickname() != null ? req.nickname() : "");
        user = userRepository.save(user);

        AccountBook book = new AccountBook();
        book.setName("日常账本");
        book.setOwnerId(user.getId());
        book.setType(0);
        accountBookRepository.save(book);

        Account account = new Account();
        account.setBookId(book.getId());
        account.setName("现金");
        account.setType(0);
        account.setIcon("💵");
        accountRepository.save(account);

        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        return new LoginResponse(token, toUserInfo(user));
    }

    public LoginResponse login(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || !passwordEncoder.matches(rawPassword, userOpt.get().getPasswordHash())) {
            throw new BizException(401, "邮箱或密码错误");
        }
        User user = userOpt.get();
        String token = jwtUtils.generateToken(user.getId(), user.getEmail());
        return new LoginResponse(token, toUserInfo(user));
    }

    public UserInfoResponse getInfo() {
        Long userId = AuthUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));
        return toUserInfo(user);
    }

    public UserInfoResponse updateInfo(UpdateUserRequest req) {
        Long userId = AuthUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException("用户不存在"));

        if (req.nickname() != null) user.setNickname(req.nickname());
        if (req.avatar() != null) user.setAvatarUrl(req.avatar());
        if (req.phone() != null) user.setPhone(req.phone());

        userRepository.save(user);
        return toUserInfo(user);
    }

    private UserInfoResponse toUserInfo(User user) {
        return new UserInfoResponse(user.getId(), user.getNickname(),
                user.getAvatarUrl(), user.getEmail(), user.getPhone());
    }
}

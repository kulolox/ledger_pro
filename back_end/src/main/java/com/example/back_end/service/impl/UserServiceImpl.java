package com.example.back_end.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.back_end.common.BizException;
import com.example.back_end.dto.request.ChangePasswordRequest;
import com.example.back_end.dto.request.UpdateUserRequest;
import com.example.back_end.dto.response.UserInfoResponse;
import com.example.back_end.entity.Account;
import com.example.back_end.entity.AccountBook;
import com.example.back_end.entity.Transaction;
import com.example.back_end.entity.User;
import com.example.back_end.entity.UserCategory;
import com.example.back_end.mapper.AccountBookMapper;
import com.example.back_end.mapper.AccountMapper;
import com.example.back_end.mapper.TransactionMapper;
import com.example.back_end.mapper.UserCategoryMapper;
import com.example.back_end.mapper.UserMapper;
import com.example.back_end.service.IUserService;
import com.example.back_end.utils.AuthUtils;
import com.example.back_end.utils.RsaUtils;
import com.example.back_end.utils.SanitizeUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired(required = false)
    private RsaUtils rsaUtils;

    @Autowired(required = false)
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private AccountBookMapper accountBookMapper;

    @Autowired(required = false)
    private AccountMapper accountMapper;

    @Autowired(required = false)
    private TransactionMapper transactionMapper;

    @Autowired(required = false)
    private UserCategoryMapper userCategoryMapper;

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

        if (req.nickname() != null) {
            String trimmed = req.nickname().trim();
            if (trimmed.isEmpty()) {
                throw new BizException("昵称不能为空");
            }
            if (trimmed.length() > 50) {
                throw new BizException("昵称长度不能超过50个字符");
            }
            user.setNickname(SanitizeUtils.escapeHtml(trimmed));
        }
        if (req.avatar() != null) {
            user.setAvatarUrl(SanitizeUtils.escapeHtml(req.avatar()));
        }
        if (req.phone() != null && !req.phone().isBlank()) {
            String phone = req.phone().trim();
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                throw new BizException("手机号格式不正确");
            }
            user.setPhone(phone);
        }

        updateById(user);
        log.info("更新用户信息 userId={}", userId);
        return toUserInfo(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest req) {
        Long userId = AuthUtils.getCurrentUserId();
        User user = getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        String rawOldPassword = rsaUtils != null ? rsaUtils.decrypt(req.old_password()) : req.old_password();
        String rawNewPassword = rsaUtils != null ? rsaUtils.decrypt(req.new_password()) : req.new_password();

        if (rawOldPassword == null || !passwordEncoder.matches(rawOldPassword, user.getPasswordHash())) {
            throw new BizException("原密码错误");
        }
        if (rawNewPassword == null || rawNewPassword.length() < 6 || rawNewPassword.length() > 32) {
            throw new BizException("新密码长度必须在6-32位之间");
        }
        if (rawNewPassword.equals(rawOldPassword)) {
            throw new BizException("新密码不能与原密码相同");
        }

        user.setPasswordHash(passwordEncoder.encode(rawNewPassword));
        updateById(user);
        log.info("用户修改密码成功 userId={}", userId);
    }

    @Override
    @Transactional
    public void deleteAccount() {
        Long userId = AuthUtils.getCurrentUserId();
        User user = getById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        if (accountBookMapper != null) {
            List<AccountBook> books = accountBookMapper.selectList(
                    new LambdaQueryWrapper<AccountBook>().eq(AccountBook::getOwnerId, userId));
            if (books != null && !books.isEmpty()) {
                List<Long> bookIds = books.stream().map(AccountBook::getId).toList();
                if (transactionMapper != null) {
                    transactionMapper.delete(new LambdaQueryWrapper<Transaction>().in(Transaction::getBookId, bookIds));
                }
                if (accountMapper != null) {
                    accountMapper.delete(new LambdaQueryWrapper<Account>().in(Account::getBookId, bookIds));
                }
                accountBookMapper.delete(new LambdaQueryWrapper<AccountBook>().eq(AccountBook::getOwnerId, userId));
            }
        }

        if (userCategoryMapper != null) {
            userCategoryMapper.delete(new LambdaQueryWrapper<UserCategory>().eq(UserCategory::getUserId, userId));
        }

        String archivedEmail = "deleted_" + userId + "_" + user.getEmail();
        if (archivedEmail.length() > 100) {
            archivedEmail = archivedEmail.substring(0, 100);
        }
        user.setEmail(archivedEmail);
        updateById(user);
        removeById(userId);
        log.info("用户注销账号成功 userId={}", userId);
    }

    private UserInfoResponse toUserInfo(User user) {
        return UserInfoResponse.from(user);
    }
}

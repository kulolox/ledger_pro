package com.example.back_end.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.back_end.entity.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {}

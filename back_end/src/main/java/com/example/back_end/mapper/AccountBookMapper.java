package com.example.back_end.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.back_end.entity.AccountBook;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountBookMapper extends BaseMapper<AccountBook> {
}

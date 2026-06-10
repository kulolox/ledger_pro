package com.example.back_end.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back_end.dto.request.CreateAccountBookRequest;
import com.example.back_end.dto.response.AccountBookResponse;
import com.example.back_end.entity.AccountBook;
import java.util.List;

public interface IAccountBookService extends IService<AccountBook> {
    List<AccountBookResponse> listByOwner();

    AccountBookResponse create(CreateAccountBookRequest req);
}

package com.example.back_end.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.back_end.dto.response.StatisticsResponse;
import com.example.back_end.dto.response.TrendResponse;
import com.example.back_end.entity.Transaction;

public interface IStatisticsService extends IService<Transaction> {
    StatisticsResponse monthly(Long bookId, String month);

    TrendResponse trend(Long bookId, int months);
}

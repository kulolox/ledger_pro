package com.example.back_end.controller;

import com.example.back_end.common.Result;
import com.example.back_end.dto.response.StatisticsResponse;
import com.example.back_end.dto.response.TrendResponse;
import com.example.back_end.service.IStatisticsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    private final IStatisticsService statisticsService;

    public StatisticsController(IStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/monthly")
    public Result<StatisticsResponse> monthly(@RequestParam Long book_id, @RequestParam String month) {
        return Result.ok(statisticsService.monthly(book_id, month));
    }

    @GetMapping("/yearly")
    public Result<StatisticsResponse> yearly(@RequestParam Long book_id, @RequestParam String year) {
        return Result.ok(statisticsService.yearly(book_id, year));
    }

    @GetMapping("/trend")
    public Result<TrendResponse> trend(@RequestParam Long book_id, @RequestParam(defaultValue = "6") int months) {
        return Result.ok(statisticsService.trend(book_id, months));
    }
}

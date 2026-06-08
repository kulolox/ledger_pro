package com.example.back_end.controller;

import com.example.back_end.dto.response.StatisticsResponse;
import com.example.back_end.service.StatisticsService;
import com.example.back_end.utils.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/monthly")
    public Result<StatisticsResponse> monthly(@RequestParam Long book_id, @RequestParam String month) {
        return Result.ok(statisticsService.monthly(book_id, month));
    }
}

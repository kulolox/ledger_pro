package com.example.back_end.controller;

import com.example.back_end.dto.response.StatisticsResponse;
import com.example.back_end.service.IStatisticsService;
import com.example.back_end.utils.Result;
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
}

package com.example.back_end.controller;

import com.example.back_end.common.Result;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        return Result.ok(Map.of("status", "UP"));
    }
}

package com.wt.test_wt.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;

    @PostMapping("/{key}")
    public String set(
            @PathVariable String key,
            @RequestParam String value) {

        redisService.set(key, value, Duration.ofMinutes(10));
        return "success";
    }

    @GetMapping("/{key}")
    public String get(@PathVariable String key) {
        return redisService.get(key);
    }
}
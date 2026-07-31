package com.example.demo;

import com.example.demo.TrinoQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trino")
public class TrinoQueryController {

    private final TrinoQueryService trinoQueryService;

    public TrinoQueryController(
            TrinoQueryService trinoQueryService) {

        this.trinoQueryService = trinoQueryService;
    }

    @GetMapping("/catalogs")
    public List<String> getCatalogs() {
        return trinoQueryService.getCatalogs();
    }

    @GetMapping("/devices")
    public List<Map<String, Object>> getDevices() {
        return trinoQueryService.getDevices();
    }
}

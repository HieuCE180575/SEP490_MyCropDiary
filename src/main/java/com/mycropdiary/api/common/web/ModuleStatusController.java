package com.mycropdiary.api.common.web;

import com.mycropdiary.api.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/public")
public class ModuleStatusController {
    @GetMapping("/status")
    ApiResponse<Map<String, String>> status() {
        return ApiResponse.success(Map.of("application", "MyCropDiary API", "status", "UP"));
    }
}

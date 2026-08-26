package com.mycropdiary.api.activity;

import com.mycropdiary.api.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/crop-seasons/{seasonId}/activities")
public class ActivityApi {
    @GetMapping
    ApiResponse<Map<String, Object>> list(@PathVariable Long seasonId) {
        return ApiResponse.success(Map.of("seasonId", seasonId, "module", "Cultivation Diary", "implementationWeek", 7));
    }
}

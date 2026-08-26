package com.mycropdiary.api.season;

import com.mycropdiary.api.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/crop-seasons")
public class SeasonApi {
    @GetMapping("/{seasonId}/dashboard")
    ApiResponse<Map<String, Object>> dashboard(@PathVariable Long seasonId) {
        return ApiResponse.success(Map.of("seasonId", seasonId, "implementationWeek", 6));
    }

    @PatchMapping("/{seasonId}/status")
    ApiResponse<Map<String, Object>> updateStatus(@PathVariable Long seasonId, @RequestBody Map<String, String> body) {
        return ApiResponse.success(Map.of("seasonId", seasonId, "requestedStatus", body.getOrDefault("status", "")));
    }
}

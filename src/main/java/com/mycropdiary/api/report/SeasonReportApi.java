package com.mycropdiary.api.report;

import com.mycropdiary.api.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/crop-seasons/{seasonId}/reports")
public class SeasonReportApi {
    @PostMapping
    ApiResponse<Map<String, Object>> generate(@PathVariable Long seasonId) {
        return ApiResponse.success(Map.of("seasonId", seasonId, "status", "QUEUED", "implementationWeek", 11));
    }
}

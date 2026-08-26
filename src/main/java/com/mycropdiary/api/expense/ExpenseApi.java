package com.mycropdiary.api.expense;

import com.mycropdiary.api.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/crop-seasons/{seasonId}")
public class ExpenseApi {
    @GetMapping("/expenses")
    ApiResponse<Map<String, Object>> expenses(@PathVariable Long seasonId) {
        return ApiResponse.success(Map.of("seasonId", seasonId, "module", "Expenses", "implementationWeek", 9));
    }

    @GetMapping("/cost-summary")
    ApiResponse<Map<String, Object>> costSummary(@PathVariable Long seasonId) {
        return ApiResponse.success(Map.of("seasonId", seasonId, "totalCost", 0));
    }
}

package com.mycropdiary.api.checklist;

import com.mycropdiary.api.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/crop-seasons/{seasonId}/checklists")
public class ChecklistApi {
    @PostMapping("/run")
    ApiResponse<Map<String, Object>> run(@PathVariable Long seasonId) {
        return ApiResponse.success(Map.of("seasonId", seasonId, "module", "Checklist", "implementationWeek", 10));
    }
}

package com.mycropdiary.api.material;

import com.mycropdiary.api.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/crop-seasons/{seasonId}/materials")
public class MaterialUsageApi {
    @GetMapping
    ApiResponse<Map<String, Object>> list(@PathVariable Long seasonId) {
        return ApiResponse.success(Map.of("seasonId", seasonId, "module", "Material Usage", "implementationWeek", 8));
    }
}

package com.mycropdiary.api.feedback;

import com.mycropdiary.api.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class FeedbackApi {
    @PostMapping("/messages/{messageId}/feedback")
    ApiResponse<Map<String, Object>> submit(@PathVariable Long messageId, @RequestBody Map<String, Object> request) {
        return ApiResponse.success(Map.of("messageId", messageId, "status", "NEW", "implementationWeek", 13));
    }
}

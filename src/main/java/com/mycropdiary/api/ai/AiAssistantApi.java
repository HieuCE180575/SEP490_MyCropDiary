package com.mycropdiary.api.ai;

import com.mycropdiary.api.common.api.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiAssistantApi {
    @PostMapping("/conversations")
    ApiResponse<Map<String, Object>> createConversation() {
        return ApiResponse.success(Map.of("module", "AI Assistant and RAG", "implementationWeek", 12));
    }

    @PostMapping("/extract-draft")
    ApiResponse<Map<String, Object>> extractDraft(@RequestBody Map<String, Object> request) {
        return ApiResponse.success(Map.of("status", "DRAFT", "requiresUserConfirmation", true));
    }
}

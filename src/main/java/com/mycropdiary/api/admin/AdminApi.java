package com.mycropdiary.api.admin;

import com.mycropdiary.api.common.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApi {
    @GetMapping("/statistics/overview")
    ApiResponse<Map<String, Object>> statistics() {
        return ApiResponse.success(Map.of("module", "Admin Statistics", "implementationWeek", 14));
    }

    @GetMapping("/crop-categories")
    ApiResponse<Map<String, Object>> cropCategories() {
        return ApiResponse.success(Map.of("module", "Crop Category Management"));
    }

    @GetMapping("/checklist-rules")
    ApiResponse<Map<String, Object>> checklistRules() {
        return ApiResponse.success(Map.of("module", "Checklist Rule Management"));
    }

    @GetMapping("/knowledge-articles")
    ApiResponse<Map<String, Object>> knowledgeArticles() {
        return ApiResponse.success(Map.of("module", "Knowledge Base Management"));
    }

    @GetMapping("/audit-logs")
    ApiResponse<Map<String, Object>> auditLogs() {
        return ApiResponse.success(Map.of("module", "Audit Logs"));
    }

    @PostMapping("/ai-feedback/{feedbackId}/replies")
    ApiResponse<Map<String, Object>> replyFeedback(@PathVariable Long feedbackId, @RequestBody Map<String, String> request) {
        return ApiResponse.success(Map.of("feedbackId", feedbackId, "replyStatus", "VISIBLE"));
    }
}

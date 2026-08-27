package com.mycropdiary.api.season.controller;

import com.mycropdiary.api.common.api.ApiResponse;
import com.mycropdiary.api.common.api.PageResponse;
import com.mycropdiary.api.season.dto.CropSeasonRequest;
import com.mycropdiary.api.season.dto.CropSeasonResponse;
import com.mycropdiary.api.season.service.CropSeasonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seasons")
public class CropSeasonController {

    private final CropSeasonService seasonService;

    public CropSeasonController(CropSeasonService seasonService) {
        this.seasonService = seasonService;
    }

    private Long resolveUserId(Long headerUserId, Long paramUserId) {
        if (paramUserId != null) {
            return paramUserId;
        }
        if (headerUserId != null) {
            return headerUserId;
        }
        return 1L;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CropSeasonResponse> create(
            @RequestHeader(value = "X-Demo-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @Valid @RequestBody CropSeasonRequest.Create request
    ) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        return ApiResponse.success("MSG-SEASON-001", "Crop season created successfully", seasonService.create(userId, request));
    }

    @GetMapping
    public ApiResponse<PageResponse<CropSeasonResponse>> findAll(
            @RequestHeader(value = "X-Demo-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        return ApiResponse.success(seasonService.findAll(userId, archived, page, Math.min(size, 100)));
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<CropSeasonResponse>> search(
            @RequestHeader(value = "X-Demo-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        return ApiResponse.success(seasonService.search(userId, keyword, page, Math.min(size, 100)));
    }

    @GetMapping("/{id}")
    public ApiResponse<CropSeasonResponse> findById(
            @RequestHeader(value = "X-Demo-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @PathVariable Long id
    ) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        return ApiResponse.success(seasonService.findById(userId, id));
    }

    @PutMapping("/{id}")
    public ApiResponse<CropSeasonResponse> update(
            @RequestHeader(value = "X-Demo-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @PathVariable Long id,
            @Valid @RequestBody CropSeasonRequest.Update request
    ) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        return ApiResponse.success("MSG-SEASON-002", "Crop season updated successfully", seasonService.update(userId, id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<CropSeasonResponse> updateStatus(
            @RequestHeader(value = "X-Demo-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @PathVariable Long id,
            @Valid @RequestBody CropSeasonRequest.UpdateStatus request
    ) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        return ApiResponse.success("MSG-SEASON-003", "Crop season status updated successfully", seasonService.updateStatus(userId, id, request.status()));
    }

    @PatchMapping("/{id}/archive")
    public ApiResponse<CropSeasonResponse> archive(
            @RequestHeader(value = "X-Demo-User-Id", required = false) Long headerUserId,
            @RequestParam(value = "userId", required = false) Long paramUserId,
            @PathVariable Long id
    ) {
        Long userId = resolveUserId(headerUserId, paramUserId);
        return ApiResponse.success("MSG-SEASON-004", "Crop season archived successfully", seasonService.archive(userId, id));
    }
}

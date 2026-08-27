package com.mycropdiary.api.landplot.controller;

import com.mycropdiary.api.common.api.ApiResponse;
import com.mycropdiary.api.common.api.PageResponse;
import com.mycropdiary.api.landplot.dto.LandPlotRequest;
import com.mycropdiary.api.landplot.dto.LandPlotResponse;
import com.mycropdiary.api.landplot.entity.LandPlotStatus;
import com.mycropdiary.api.landplot.service.LandPlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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

@Validated
@RestController
@RequestMapping("/land-plots")
@Tag(name = "Land Plots", description = "Manage the authenticated user's cultivation land plots")
public class LandPlotController {

    private final LandPlotService service;

    public LandPlotController(LandPlotService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List, search and filter land plots")
    public ApiResponse<PageResponse<LandPlotResponse>> findAll(
            @Parameter(description = "Temporary authenticated-user header until the JWT filter is implemented")
            @RequestHeader("X-Demo-User-Id") @Positive Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "ACTIVE") LandPlotStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return ApiResponse.success(service.findAll(userId, keyword, status, page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    @Operation(summary = "View a land plot owned by the current user")
    public ApiResponse<LandPlotResponse> findById(
            @RequestHeader("X-Demo-User-Id") @Positive Long userId,
            @PathVariable @Positive Long id) {
        return ApiResponse.success(service.findById(userId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a land plot")
    public ApiResponse<LandPlotResponse> create(
            @RequestHeader("X-Demo-User-Id") @Positive Long userId,
            @Valid @RequestBody LandPlotRequest.Create request) {
        return ApiResponse.success(
                "MSG-LAND-PLOT-001",
                "Land plot created successfully",
                service.create(userId, request)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a land plot")
    public ApiResponse<LandPlotResponse> update(
            @RequestHeader("X-Demo-User-Id") @Positive Long userId,
            @PathVariable @Positive Long id,
            @Valid @RequestBody LandPlotRequest.Update request) {
        return ApiResponse.success(
                "MSG-LAND-PLOT-002",
                "Land plot updated successfully",
                service.update(userId, id, request)
        );
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activate or deactivate a land plot")
    public ApiResponse<LandPlotResponse> updateStatus(
            @RequestHeader("X-Demo-User-Id") @Positive Long userId,
            @PathVariable @Positive Long id,
            @Valid @RequestBody LandPlotRequest.UpdateStatus request) {
        return ApiResponse.success(
                "MSG-LAND-PLOT-003",
                "Land plot status updated successfully",
                service.updateStatus(userId, id, request.status())
        );
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive a land plot without deleting its history")
    public ApiResponse<LandPlotResponse> archive(
            @RequestHeader("X-Demo-User-Id") @Positive Long userId,
            @PathVariable @Positive Long id) {
        return ApiResponse.success(
                "MSG-LAND-PLOT-004",
                "Land plot archived successfully",
                service.archive(userId, id)
        );
    }
}

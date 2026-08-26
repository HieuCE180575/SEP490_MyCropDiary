package com.mycropdiary.api.landplot;

import com.mycropdiary.api.common.api.ApiResponse;
import com.mycropdiary.api.common.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/land-plots")
public class LandPlotController {
    private final LandPlotService service;

    public LandPlotController(LandPlotService service) {
        this.service = service;
    }

    @GetMapping
    ApiResponse<PageResponse<LandPlotService.LandPlotResponse>> findAll(
            @RequestHeader("X-Demo-User-Id") Long userId,
            @RequestParam(defaultValue = "ACTIVE") LandPlot.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(service.findAll(userId, status, page, Math.min(size, 100)));
    }

    @GetMapping("/{id}")
    ApiResponse<LandPlotService.LandPlotResponse> findOne(@RequestHeader("X-Demo-User-Id") Long userId,
                                                          @PathVariable Long id) {
        return ApiResponse.success(service.findOne(userId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<LandPlotService.LandPlotResponse> create(@RequestHeader("X-Demo-User-Id") Long userId,
                                                         @Valid @RequestBody LandPlotService.CreateLandPlotRequest request) {
        return ApiResponse.success("MSG-008", "Land plot created successfully", service.create(userId, request));
    }
}

package com.mycropdiary.api.landplot.dto;

import com.mycropdiary.api.landplot.entity.AreaUnit;
import com.mycropdiary.api.landplot.entity.LandPlotStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record LandPlotResponse(
        Long id,
        String plotCode,
        String name,
        BigDecimal areaValue,
        AreaUnit areaUnit,
        String address,
        BigDecimal latitude,
        BigDecimal longitude,
        String soilType,
        String waterSource,
        String notes,
        LandPlotStatus status,
        Instant archivedAt,
        Instant createdAt,
        Instant updatedAt
) {
}

package com.mycropdiary.api.season.dto;

import com.mycropdiary.api.season.entity.SeasonStatus;

import java.time.Instant;
import java.time.LocalDate;

public record CropSeasonResponse(
        Long id,
        Long userId,
        Long landPlotId,
        Long cropCategoryId,
        String seasonCode,
        String seasonName,
        LocalDate expectedStartDate,
        LocalDate expectedEndDate,
        LocalDate actualStartDate,
        LocalDate actualEndDate,
        SeasonStatus status,
        String notes,
        boolean archived,
        Instant createdAt,
        Instant updatedAt
) {}

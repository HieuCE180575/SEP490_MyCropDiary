package com.mycropdiary.api.season.dto;

import com.mycropdiary.api.season.entity.SeasonStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CropSeasonRequest {

    public record Create(
            @NotBlank(message = "Season name is required")
            @Size(max = 180, message = "Season name must not exceed 180 characters")
            String seasonName,

            @NotNull(message = "Land plot ID is required")
            Long landPlotId,

            @NotNull(message = "Crop category ID is required")
            Long cropCategoryId,

            @NotNull(message = "Expected start date is required")
            LocalDate expectedStartDate,

            @NotNull(message = "Expected end date is required")
            LocalDate expectedEndDate,

            String notes
    ) {}

    public record Update(
            @Size(max = 180, message = "Season name must not exceed 180 characters")
            String seasonName,

            LocalDate expectedStartDate,

            LocalDate expectedEndDate,

            String notes
    ) {}

    public record UpdateStatus(
            @NotNull(message = "Status is required")
            SeasonStatus status
    ) {}
}

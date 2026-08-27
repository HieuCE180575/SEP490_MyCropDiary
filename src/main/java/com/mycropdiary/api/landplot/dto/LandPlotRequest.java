package com.mycropdiary.api.landplot.dto;

import com.mycropdiary.api.landplot.entity.AreaUnit;
import com.mycropdiary.api.landplot.entity.LandPlotStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class LandPlotRequest {

    private LandPlotRequest() {
    }

    public record Create(
            @NotBlank(message = "Plot code is required")
            @Size(max = 50, message = "Plot code must not exceed 50 characters")
            @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Plot code may contain only letters, numbers, hyphens and underscores")
            @Schema(example = "PLOT-001")
            String plotCode,

            @NotBlank(message = "Plot name is required")
            @Size(max = 150, message = "Plot name must not exceed 150 characters")
            @Schema(example = "North Vegetable Plot")
            String name,

            @NotNull(message = "Area value is required")
            @DecimalMin(value = "0.001", message = "Area value must be greater than zero")
            @Digits(integer = 11, fraction = 3, message = "Area value supports at most 11 integer and 3 decimal digits")
            @Schema(example = "1250.500")
            BigDecimal areaValue,

            @NotNull(message = "Area unit is required")
            AreaUnit areaUnit,

            @Size(max = 500, message = "Address must not exceed 500 characters")
            String address,

            @DecimalMin(value = "-90.0000000", message = "Latitude must be between -90 and 90")
            @DecimalMax(value = "90.0000000", message = "Latitude must be between -90 and 90")
            @Digits(integer = 2, fraction = 7, message = "Latitude supports at most 7 decimal digits")
            BigDecimal latitude,

            @DecimalMin(value = "-180.0000000", message = "Longitude must be between -180 and 180")
            @DecimalMax(value = "180.0000000", message = "Longitude must be between -180 and 180")
            @Digits(integer = 3, fraction = 7, message = "Longitude supports at most 7 decimal digits")
            BigDecimal longitude,

            @Size(max = 100, message = "Soil type must not exceed 100 characters")
            String soilType,

            @Size(max = 150, message = "Water source must not exceed 150 characters")
            String waterSource,

            @Size(max = 10000, message = "Notes must not exceed 10000 characters")
            String notes
    ) {
    }

    public record Update(
            @NotBlank(message = "Plot code is required")
            @Size(max = 50, message = "Plot code must not exceed 50 characters")
            @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Plot code may contain only letters, numbers, hyphens and underscores")
            String plotCode,

            @NotBlank(message = "Plot name is required")
            @Size(max = 150, message = "Plot name must not exceed 150 characters")
            String name,

            @NotNull(message = "Area value is required")
            @DecimalMin(value = "0.001", message = "Area value must be greater than zero")
            @Digits(integer = 11, fraction = 3, message = "Area value supports at most 11 integer and 3 decimal digits")
            BigDecimal areaValue,

            @NotNull(message = "Area unit is required")
            AreaUnit areaUnit,

            @Size(max = 500, message = "Address must not exceed 500 characters")
            String address,

            @DecimalMin(value = "-90.0000000", message = "Latitude must be between -90 and 90")
            @DecimalMax(value = "90.0000000", message = "Latitude must be between -90 and 90")
            @Digits(integer = 2, fraction = 7, message = "Latitude supports at most 7 decimal digits")
            BigDecimal latitude,

            @DecimalMin(value = "-180.0000000", message = "Longitude must be between -180 and 180")
            @DecimalMax(value = "180.0000000", message = "Longitude must be between -180 and 180")
            @Digits(integer = 3, fraction = 7, message = "Longitude supports at most 7 decimal digits")
            BigDecimal longitude,

            @Size(max = 100, message = "Soil type must not exceed 100 characters")
            String soilType,

            @Size(max = 150, message = "Water source must not exceed 150 characters")
            String waterSource,

            @Size(max = 10000, message = "Notes must not exceed 10000 characters")
            String notes
    ) {
    }

    public record UpdateStatus(
            @NotNull(message = "Status is required")
            LandPlotStatus status
    ) {
    }
}

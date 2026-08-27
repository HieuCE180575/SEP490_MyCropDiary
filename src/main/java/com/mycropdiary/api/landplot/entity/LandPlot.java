package com.mycropdiary.api.landplot.entity;

import com.mycropdiary.api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
        name = "land_plots",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_land_plots_user_code",
                columnNames = {"user_id", "plot_code"}
        ),
        indexes = {
                @Index(name = "idx_land_plots_user_status", columnList = "user_id,status"),
                @Index(name = "idx_land_plots_user_name", columnList = "user_id,name")
        }
)
public class LandPlot extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plot_code", nullable = false, length = 50)
    private String plotCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "area_value", nullable = false, precision = 14, scale = 3)
    private BigDecimal areaValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "area_unit", nullable = false, length = 20)
    private AreaUnit areaUnit = AreaUnit.M2;

    @Column(length = 500)
    private String address;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "soil_type", length = 100)
    private String soilType;

    @Column(name = "water_source", length = 150)
    private String waterSource;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LandPlotStatus status = LandPlotStatus.ACTIVE;

    @Column(name = "archived_at")
    private Instant archivedAt;
}

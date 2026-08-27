package com.mycropdiary.api.season.entity;

import com.mycropdiary.api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "crop_seasons")
public class CropSeason extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "land_plot_id", nullable = false)
    private Long landPlotId;

    @Column(name = "crop_category_id", nullable = false)
    private Long cropCategoryId;

    @Column(name = "season_code", length = 50)
    private String seasonCode;

    @Column(name = "name", nullable = false, length = 180)
    private String seasonName;

    @Column(name = "expected_start_date", nullable = false)
    private LocalDate expectedStartDate;

    @Column(name = "expected_end_date", nullable = false)
    private LocalDate expectedEndDate;

    @Column(name = "actual_start_date")
    private LocalDate actualStartDate;

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SeasonStatus status = SeasonStatus.PLANNED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private boolean archived = false;
}

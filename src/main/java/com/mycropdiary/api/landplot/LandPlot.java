package com.mycropdiary.api.landplot;

import com.mycropdiary.api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "land_plots")
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
    @Column(name = "area_unit", nullable = false)
    private AreaUnit areaUnit = AreaUnit.M2;
    @Column(length = 500)
    private String address;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    public enum AreaUnit { M2, HECTARE }
    public enum Status { ACTIVE, INACTIVE, ARCHIVED }
}

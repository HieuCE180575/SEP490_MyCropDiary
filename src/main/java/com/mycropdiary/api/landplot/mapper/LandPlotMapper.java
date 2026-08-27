package com.mycropdiary.api.landplot.mapper;

import com.mycropdiary.api.landplot.dto.LandPlotRequest;
import com.mycropdiary.api.landplot.dto.LandPlotResponse;
import com.mycropdiary.api.landplot.entity.AreaUnit;
import com.mycropdiary.api.landplot.entity.LandPlot;
import com.mycropdiary.api.landplot.entity.LandPlotStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;

@Component
public class LandPlotMapper {

    public LandPlot toEntity(Long userId, LandPlotRequest.Create request) {
        LandPlot entity = new LandPlot();
        entity.setUserId(userId);
        entity.setStatus(LandPlotStatus.ACTIVE);
        apply(entity, request.plotCode(), request.name(), request.areaValue(), request.areaUnit(),
                request.address(), request.latitude(), request.longitude(), request.soilType(),
                request.waterSource(), request.notes());
        return entity;
    }

    public void updateEntity(LandPlot entity, LandPlotRequest.Update request) {
        apply(entity, request.plotCode(), request.name(), request.areaValue(), request.areaUnit(),
                request.address(), request.latitude(), request.longitude(), request.soilType(),
                request.waterSource(), request.notes());
    }

    public LandPlotResponse toResponse(LandPlot entity) {
        return new LandPlotResponse(
                entity.getId(),
                entity.getPlotCode(),
                entity.getName(),
                entity.getAreaValue(),
                entity.getAreaUnit(),
                entity.getAddress(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getSoilType(),
                entity.getWaterSource(),
                entity.getNotes(),
                entity.getStatus(),
                entity.getArchivedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private void apply(LandPlot entity, String plotCode, String name,
                       BigDecimal areaValue, AreaUnit areaUnit,
                       String address, BigDecimal latitude,
                       BigDecimal longitude, String soilType,
                       String waterSource, String notes) {
        entity.setPlotCode(plotCode.trim().toUpperCase(Locale.ROOT));
        entity.setName(name.trim());
        entity.setAreaValue(areaValue);
        entity.setAreaUnit(areaUnit);
        entity.setAddress(trimToNull(address));
        entity.setLatitude(latitude);
        entity.setLongitude(longitude);
        entity.setSoilType(trimToNull(soilType));
        entity.setWaterSource(trimToNull(waterSource));
        entity.setNotes(trimToNull(notes));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

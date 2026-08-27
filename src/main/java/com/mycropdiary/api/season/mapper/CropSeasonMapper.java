package com.mycropdiary.api.season.mapper;

import com.mycropdiary.api.season.dto.CropSeasonRequest;
import com.mycropdiary.api.season.dto.CropSeasonResponse;
import com.mycropdiary.api.season.entity.CropSeason;
import com.mycropdiary.api.season.entity.SeasonStatus;
import org.springframework.stereotype.Component;

@Component
public class CropSeasonMapper {

    public CropSeason toEntity(Long userId, CropSeasonRequest.Create request) {
        CropSeason entity = new CropSeason();
        entity.setUserId(userId);
        entity.setLandPlotId(request.landPlotId());
        entity.setCropCategoryId(request.cropCategoryId());
        entity.setSeasonName(request.seasonName() != null ? request.seasonName().trim() : null);
        entity.setExpectedStartDate(request.expectedStartDate());
        entity.setExpectedEndDate(request.expectedEndDate());
        entity.setNotes(request.notes());
        entity.setStatus(SeasonStatus.PLANNED);
        entity.setArchived(false);
        return entity;
    }

    public CropSeasonResponse toResponse(CropSeason entity) {
        if (entity == null) {
            return null;
        }
        return new CropSeasonResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getLandPlotId(),
                entity.getCropCategoryId(),
                entity.getSeasonCode(),
                entity.getSeasonName(),
                entity.getExpectedStartDate(),
                entity.getExpectedEndDate(),
                entity.getActualStartDate(),
                entity.getActualEndDate(),
                entity.getStatus(),
                entity.getNotes(),
                entity.isArchived(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

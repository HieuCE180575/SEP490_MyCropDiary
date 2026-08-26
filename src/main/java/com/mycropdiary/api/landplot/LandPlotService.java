package com.mycropdiary.api.landplot;

import com.mycropdiary.api.common.api.PageResponse;
import com.mycropdiary.api.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class LandPlotService {
    private final LandPlotRepository repository;

    public LandPlotService(LandPlotRepository repository) {
        this.repository = repository;
    }

    public PageResponse<LandPlotResponse> findAll(Long userId, LandPlot.Status status, int page, int size) {
        return PageResponse.from(repository.findByUserIdAndStatus(userId, status, PageRequest.of(page, size))
                .map(LandPlotResponse::from));
    }

    public LandPlotResponse findOne(Long userId, Long id) {
        return LandPlotResponse.from(repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Land plot", id)));
    }

    @Transactional
    public LandPlotResponse create(Long userId, CreateLandPlotRequest request) {
        LandPlot entity = new LandPlot();
        entity.setUserId(userId);
        entity.setPlotCode(request.plotCode().trim());
        entity.setName(request.name().trim());
        entity.setAreaValue(request.areaValue());
        entity.setAreaUnit(request.areaUnit());
        entity.setAddress(request.address());
        entity.setNotes(request.notes());
        return LandPlotResponse.from(repository.save(entity));
    }

    public record CreateLandPlotRequest(String plotCode, String name, BigDecimal areaValue,
                                        LandPlot.AreaUnit areaUnit, String address, String notes) {}

    public record LandPlotResponse(Long id, String plotCode, String name, BigDecimal areaValue,
                                   LandPlot.AreaUnit areaUnit, String address, String notes, LandPlot.Status status) {
        static LandPlotResponse from(LandPlot entity) {
            return new LandPlotResponse(entity.getId(), entity.getPlotCode(), entity.getName(), entity.getAreaValue(),
                    entity.getAreaUnit(), entity.getAddress(), entity.getNotes(), entity.getStatus());
        }
    }
}

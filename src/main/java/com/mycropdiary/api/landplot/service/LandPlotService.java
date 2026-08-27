package com.mycropdiary.api.landplot.service;

import com.mycropdiary.api.common.api.PageResponse;
import com.mycropdiary.api.landplot.dto.LandPlotRequest;
import com.mycropdiary.api.landplot.dto.LandPlotResponse;
import com.mycropdiary.api.landplot.entity.LandPlotStatus;
import org.springframework.data.domain.Sort;

public interface LandPlotService {

    PageResponse<LandPlotResponse> findAll(Long userId, String keyword, LandPlotStatus status,
                                           int page, int size, String sortBy, Sort.Direction direction);

    LandPlotResponse findById(Long userId, Long id);

    LandPlotResponse create(Long userId, LandPlotRequest.Create request);

    LandPlotResponse update(Long userId, Long id, LandPlotRequest.Update request);

    LandPlotResponse updateStatus(Long userId, Long id, LandPlotStatus status);

    LandPlotResponse archive(Long userId, Long id);
}

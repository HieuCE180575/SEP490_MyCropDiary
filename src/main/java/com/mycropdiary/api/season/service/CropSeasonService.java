package com.mycropdiary.api.season.service;

import com.mycropdiary.api.common.api.PageResponse;
import com.mycropdiary.api.season.dto.CropSeasonRequest;
import com.mycropdiary.api.season.dto.CropSeasonResponse;
import com.mycropdiary.api.season.entity.SeasonStatus;

public interface CropSeasonService {

    CropSeasonResponse create(Long userId, CropSeasonRequest.Create request);

    PageResponse<CropSeasonResponse> findAll(Long userId, Boolean archived, int page, int size);

    CropSeasonResponse findById(Long userId, Long id);

    CropSeasonResponse update(Long userId, Long id, CropSeasonRequest.Update request);

    PageResponse<CropSeasonResponse> search(Long userId, String keyword, int page, int size);

    CropSeasonResponse updateStatus(Long userId, Long id, SeasonStatus newStatus);

    CropSeasonResponse archive(Long userId, Long id);
}

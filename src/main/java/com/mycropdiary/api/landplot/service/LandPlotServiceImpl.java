package com.mycropdiary.api.landplot.service;

import com.mycropdiary.api.common.api.PageResponse;
import com.mycropdiary.api.common.exception.BusinessException;
import com.mycropdiary.api.common.exception.ResourceNotFoundException;
import com.mycropdiary.api.landplot.dto.LandPlotRequest;
import com.mycropdiary.api.landplot.dto.LandPlotResponse;
import com.mycropdiary.api.landplot.entity.LandPlot;
import com.mycropdiary.api.landplot.entity.LandPlotStatus;
import com.mycropdiary.api.landplot.mapper.LandPlotMapper;
import com.mycropdiary.api.landplot.repository.LandPlotRepository;
import com.mycropdiary.api.season.entity.SeasonStatus;
import com.mycropdiary.api.season.repository.CropSeasonRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class LandPlotServiceImpl implements LandPlotService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "updatedAt", "plotCode", "name", "areaValue", "status"
    );

    private final LandPlotRepository repository;
    private final CropSeasonRepository cropSeasonRepository;
    private final LandPlotMapper mapper;

    public LandPlotServiceImpl(LandPlotRepository repository,
                               CropSeasonRepository cropSeasonRepository,
                               LandPlotMapper mapper) {
        this.repository = repository;
        this.cropSeasonRepository = cropSeasonRepository;
        this.mapper = mapper;
    }

    @Override
    public PageResponse<LandPlotResponse> findAll(Long userId, String keyword, LandPlotStatus status,
                                                  int page, int size, String sortBy, Sort.Direction direction) {
        validateUserId(userId);
        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "updatedAt";
        String cleanKeyword = trimToNull(keyword);
        Sort.Direction safeDirection = direction != null ? direction : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(safeDirection, safeSortBy));
        return PageResponse.from(repository.searchOwnedPlots(userId, cleanKeyword, status, pageable)
                .map(mapper::toResponse));
    }

    @Override
    public LandPlotResponse findById(Long userId, Long id) {
        return mapper.toResponse(findOwnedPlot(userId, id));
    }

    @Override
    @Transactional
    public LandPlotResponse create(Long userId, LandPlotRequest.Create request) {
        validateUserId(userId);
        String normalizedCode = normalizeCode(request.plotCode());
        if (repository.existsByUserIdAndPlotCodeIgnoreCase(userId, normalizedCode)) {
            throw duplicatePlotCode(normalizedCode);
        }

        LandPlot entity = mapper.toEntity(userId, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public LandPlotResponse update(Long userId, Long id, LandPlotRequest.Update request) {
        LandPlot entity = findOwnedPlot(userId, id);
        ensureNotArchived(entity);

        String normalizedCode = normalizeCode(request.plotCode());
        if (repository.existsByUserIdAndPlotCodeIgnoreCaseAndIdNot(userId, normalizedCode, id)) {
            throw duplicatePlotCode(normalizedCode);
        }

        mapper.updateEntity(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public LandPlotResponse updateStatus(Long userId, Long id, LandPlotStatus newStatus) {
        LandPlot entity = findOwnedPlot(userId, id);
        ensureNotArchived(entity);

        if (newStatus == null) {
            throw new BusinessException(
                    "LAND_PLOT_STATUS_REQUIRED",
                    "Land plot status is required",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (newStatus == LandPlotStatus.ARCHIVED) {
            throw new BusinessException(
                    "LAND_PLOT_USE_ARCHIVE_ENDPOINT",
                    "Use the archive endpoint to archive a land plot",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (newStatus == LandPlotStatus.INACTIVE && hasOpenSeason(id)) {
            throw new BusinessException(
                    "LAND_PLOT_HAS_OPEN_SEASON",
                    "Cannot deactivate a land plot that has a planned or in-progress crop season",
                    HttpStatus.CONFLICT
            );
        }

        entity.setStatus(newStatus);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional
    public LandPlotResponse archive(Long userId, Long id) {
        LandPlot entity = findOwnedPlot(userId, id);
        if (entity.getStatus() == LandPlotStatus.ARCHIVED) {
            return mapper.toResponse(entity);
        }

        if (hasOpenSeason(id)) {
            throw new BusinessException(
                    "LAND_PLOT_HAS_OPEN_SEASON",
                    "Cannot archive a land plot that has a planned or in-progress crop season",
                    HttpStatus.CONFLICT
            );
        }

        entity.setStatus(LandPlotStatus.ARCHIVED);
        entity.setArchivedAt(Instant.now());
        return mapper.toResponse(repository.save(entity));
    }

    private LandPlot findOwnedPlot(Long userId, Long id) {
        validateUserId(userId);
        if (id == null || id <= 0) {
            throw new ResourceNotFoundException("Land plot", id);
        }
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Land plot", id));
    }

    private void ensureNotArchived(LandPlot entity) {
        if (entity.getStatus() == LandPlotStatus.ARCHIVED) {
            throw new BusinessException(
                    "LAND_PLOT_ARCHIVED",
                    "Archived land plots cannot be modified",
                    HttpStatus.CONFLICT
            );
        }
    }

    private boolean hasOpenSeason(Long landPlotId) {
        return cropSeasonRepository.existsByLandPlotIdAndStatusIn(
                landPlotId,
                List.of(SeasonStatus.PLANNED, SeasonStatus.IN_PROGRESS)
        );
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(
                    "AUTHENTICATION_REQUIRED",
                    "A valid authenticated user is required",
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    private BusinessException duplicatePlotCode(String plotCode) {
        return new BusinessException(
                "LAND_PLOT_CODE_EXISTS",
                "Land plot code already exists: " + plotCode,
                HttpStatus.CONFLICT
        );
    }

    private String normalizeCode(String plotCode) {
        return plotCode.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

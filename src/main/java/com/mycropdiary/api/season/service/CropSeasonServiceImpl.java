package com.mycropdiary.api.season.service;

import com.mycropdiary.api.common.api.PageResponse;
import com.mycropdiary.api.common.exception.BusinessException;
import com.mycropdiary.api.common.exception.ResourceNotFoundException;
import com.mycropdiary.api.season.dto.CropSeasonRequest;
import com.mycropdiary.api.season.dto.CropSeasonResponse;
import com.mycropdiary.api.season.entity.CropSeason;
import com.mycropdiary.api.season.entity.SeasonStatus;
import com.mycropdiary.api.season.mapper.CropSeasonMapper;
import com.mycropdiary.api.season.repository.CropSeasonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class CropSeasonServiceImpl implements CropSeasonService {

    private final CropSeasonRepository repository;
    private final CropSeasonMapper mapper;

    public CropSeasonServiceImpl(CropSeasonRepository repository, CropSeasonMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public CropSeasonResponse create(Long userId, CropSeasonRequest.Create request) {
        if (request.expectedEndDate().isBefore(request.expectedStartDate())) {
            throw new BusinessException(
                    "INVALID_DATE_RANGE",
                    "Expected end date cannot be before expected start date",
                    HttpStatus.BAD_REQUEST
            );
        }

        CropSeason entity = mapper.toEntity(userId, request);
        if (entity.getSeasonCode() == null || entity.getSeasonCode().isBlank()) {
            entity.setSeasonCode("CS-" + System.currentTimeMillis());
        }
        CropSeason saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public PageResponse<CropSeasonResponse> findAll(Long userId, Boolean archived, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CropSeason> pageResult;
        if (archived != null) {
            pageResult = repository.findByUserIdAndArchived(userId, archived, pageRequest);
        } else {
            pageResult = repository.findByUserId(userId, pageRequest);
        }
        return PageResponse.from(pageResult.map(mapper::toResponse));
    }

    @Override
    public CropSeasonResponse findById(Long userId, Long id) {
        CropSeason entity = findEntityByIdAndUserId(id, userId);
        return mapper.toResponse(entity);
    }

    @Override
    @Transactional
    public CropSeasonResponse update(Long userId, Long id, CropSeasonRequest.Update request) {
        CropSeason entity = findEntityByIdAndUserId(id, userId);

        LocalDate newStartDate = request.expectedStartDate() != null ? request.expectedStartDate() : entity.getExpectedStartDate();
        LocalDate newEndDate = request.expectedEndDate() != null ? request.expectedEndDate() : entity.getExpectedEndDate();

        if (newEndDate.isBefore(newStartDate)) {
            throw new BusinessException(
                    "INVALID_DATE_RANGE",
                    "Expected end date cannot be before expected start date",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (request.seasonName() != null && !request.seasonName().isBlank()) {
            entity.setSeasonName(request.seasonName().trim());
        }
        if (request.expectedStartDate() != null) {
            entity.setExpectedStartDate(request.expectedStartDate());
        }
        if (request.expectedEndDate() != null) {
            entity.setExpectedEndDate(request.expectedEndDate());
        }
        if (request.notes() != null) {
            entity.setNotes(request.notes());
        }

        CropSeason updated = repository.save(entity);
        return mapper.toResponse(updated);
    }

    @Override
    public PageResponse<CropSeasonResponse> search(Long userId, String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String cleanKeyword = keyword != null ? keyword.trim() : "";
        Page<CropSeason> pageResult = repository.searchByKeyword(userId, cleanKeyword, pageRequest);
        return PageResponse.from(pageResult.map(mapper::toResponse));
    }

    @Override
    @Transactional
    public CropSeasonResponse updateStatus(Long userId, Long id, SeasonStatus newStatus) {
        CropSeason entity = findEntityByIdAndUserId(id, userId);
        SeasonStatus currentStatus = entity.getStatus();

        if (currentStatus == newStatus) {
            return mapper.toResponse(entity);
        }

        validateStatusTransition(currentStatus, newStatus);

        entity.setStatus(newStatus);

        if (currentStatus == SeasonStatus.PLANNED && newStatus == SeasonStatus.IN_PROGRESS) {
            if (entity.getActualStartDate() == null) {
                entity.setActualStartDate(LocalDate.now());
            }
        } else if (currentStatus == SeasonStatus.IN_PROGRESS && newStatus == SeasonStatus.COMPLETED) {
            if (entity.getActualEndDate() == null) {
                entity.setActualEndDate(LocalDate.now());
            }
        }

        CropSeason saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CropSeasonResponse archive(Long userId, Long id) {
        CropSeason entity = findEntityByIdAndUserId(id, userId);

        if (entity.getStatus() != SeasonStatus.COMPLETED && entity.getStatus() != SeasonStatus.CANCELLED) {
            throw new BusinessException(
                    "ARCHIVE_NOT_ALLOWED",
                    "Cannot archive season with status '" + entity.getStatus() + "'. Only COMPLETED or CANCELLED seasons can be archived.",
                    HttpStatus.BAD_REQUEST
            );
        }

        entity.setArchived(true);
        CropSeason saved = repository.save(entity);
        return mapper.toResponse(saved);
    }

    private CropSeason findEntityByIdAndUserId(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Crop season", id));
    }

    private void validateStatusTransition(SeasonStatus currentStatus, SeasonStatus newStatus) {
        switch (currentStatus) {
            case PLANNED:
                if (newStatus != SeasonStatus.IN_PROGRESS && newStatus != SeasonStatus.CANCELLED) {
                    throw new BusinessException(
                            "INVALID_STATUS_TRANSITION",
                            "From PLANNED, status can only transition to IN_PROGRESS or CANCELLED",
                            HttpStatus.BAD_REQUEST
                    );
                }
                break;

            case IN_PROGRESS:
                if (newStatus != SeasonStatus.COMPLETED && newStatus != SeasonStatus.CANCELLED) {
                    throw new BusinessException(
                            "INVALID_STATUS_TRANSITION",
                            "From IN_PROGRESS, status can only transition to COMPLETED or CANCELLED",
                            HttpStatus.BAD_REQUEST
                    );
                }
                break;

            case COMPLETED:
            case CANCELLED:
                throw new BusinessException(
                        "INVALID_STATUS_TRANSITION",
                        "Cannot change status of a season that is already " + currentStatus,
                        HttpStatus.BAD_REQUEST
                );

            default:
                throw new BusinessException(
                        "INVALID_STATUS_TRANSITION",
                        "Unsupported status transition from " + currentStatus + " to " + newStatus,
                        HttpStatus.BAD_REQUEST
                );
        }
    }
}

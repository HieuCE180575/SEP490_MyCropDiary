package com.mycropdiary.api.landplot.service;

import com.mycropdiary.api.common.exception.BusinessException;
import com.mycropdiary.api.common.exception.ResourceNotFoundException;
import com.mycropdiary.api.landplot.dto.LandPlotRequest;
import com.mycropdiary.api.landplot.dto.LandPlotResponse;
import com.mycropdiary.api.landplot.entity.AreaUnit;
import com.mycropdiary.api.landplot.entity.LandPlot;
import com.mycropdiary.api.landplot.entity.LandPlotStatus;
import com.mycropdiary.api.landplot.mapper.LandPlotMapper;
import com.mycropdiary.api.landplot.repository.LandPlotRepository;
import com.mycropdiary.api.season.entity.SeasonStatus;
import com.mycropdiary.api.season.repository.CropSeasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LandPlotServiceImplTest {

    @Mock
    private LandPlotRepository repository;

    @Mock
    private CropSeasonRepository cropSeasonRepository;

    private LandPlotServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new LandPlotServiceImpl(repository, cropSeasonRepository, new LandPlotMapper());
    }

    @Test
    void createNormalizesCodeAndSavesOwnedPlot() {
        LandPlotRequest.Create request = createRequest(" plot-001 ");
        when(repository.existsByUserIdAndPlotCodeIgnoreCase(7L, "PLOT-001")).thenReturn(false);
        when(repository.save(any(LandPlot.class))).thenAnswer(invocation -> {
            LandPlot entity = invocation.getArgument(0);
            entity.setId(11L);
            entity.setCreatedAt(Instant.parse("2026-08-27T10:00:00Z"));
            entity.setUpdatedAt(Instant.parse("2026-08-27T10:00:00Z"));
            return entity;
        });

        LandPlotResponse response = service.create(7L, request);

        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.plotCode()).isEqualTo("PLOT-001");
        assertThat(response.name()).isEqualTo("North Plot");
        assertThat(response.status()).isEqualTo(LandPlotStatus.ACTIVE);

        ArgumentCaptor<LandPlot> captor = ArgumentCaptor.forClass(LandPlot.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(7L);
    }

    @Test
    void createRejectsDuplicateCodeForSameOwner() {
        LandPlotRequest.Create request = createRequest("PLOT-001");
        when(repository.existsByUserIdAndPlotCodeIgnoreCase(7L, "PLOT-001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(7L, request))
                .isInstanceOfSatisfying(BusinessException.class, ex -> {
                    assertThat(ex.getMessageCode()).isEqualTo("LAND_PLOT_CODE_EXISTS");
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                });

        verify(repository, never()).save(any());
    }

    @Test
    void findByIdDoesNotExposeAnotherUsersPlot() {
        when(repository.findByIdAndUserId(99L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(7L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllUsesOwnerKeywordStatusPaginationAndSafeSort() {
        LandPlot entity = ownedPlot(7L, 11L, LandPlotStatus.ACTIVE);
        when(repository.searchOwnedPlots(eq(7L), eq("north"), eq(LandPlotStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        var response = service.findAll(7L, " north ", LandPlotStatus.ACTIVE,
                0, 10, "unsupportedField", Sort.Direction.DESC);

        assertThat(response.content()).hasSize(1);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).searchOwnedPlots(eq(7L), eq("north"), eq(LandPlotStatus.ACTIVE), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("updatedAt")).isNotNull();
    }

    @Test
    void updateRejectsArchivedPlot() {
        LandPlot archived = ownedPlot(7L, 11L, LandPlotStatus.ARCHIVED);
        when(repository.findByIdAndUserId(11L, 7L)).thenReturn(Optional.of(archived));

        assertThatThrownBy(() -> service.update(7L, 11L, updateRequest()))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getMessageCode()).isEqualTo("LAND_PLOT_ARCHIVED"));

        verify(repository, never()).save(any());
    }

    @Test
    void archiveRejectsPlotWithOpenSeason() {
        LandPlot active = ownedPlot(7L, 11L, LandPlotStatus.ACTIVE);
        when(repository.findByIdAndUserId(11L, 7L)).thenReturn(Optional.of(active));
        when(cropSeasonRepository.existsByLandPlotIdAndStatusIn(
                eq(11L), eq(List.of(SeasonStatus.PLANNED, SeasonStatus.IN_PROGRESS))))
                .thenReturn(true);

        assertThatThrownBy(() -> service.archive(7L, 11L))
                .isInstanceOfSatisfying(BusinessException.class, ex -> {
                    assertThat(ex.getMessageCode()).isEqualTo("LAND_PLOT_HAS_OPEN_SEASON");
                    assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                });

        verify(repository, never()).save(any());
    }

    @Test
    void archivePreservesHistoryAndMarksTimestamp() {
        LandPlot active = ownedPlot(7L, 11L, LandPlotStatus.ACTIVE);
        when(repository.findByIdAndUserId(11L, 7L)).thenReturn(Optional.of(active));
        when(cropSeasonRepository.existsByLandPlotIdAndStatusIn(eq(11L), any())).thenReturn(false);
        when(repository.save(active)).thenReturn(active);

        LandPlotResponse response = service.archive(7L, 11L);

        assertThat(response.status()).isEqualTo(LandPlotStatus.ARCHIVED);
        assertThat(response.archivedAt()).isNotNull();
        verify(repository).save(active);
    }

    @Test
    void statusEndpointCannotBeUsedToArchive() {
        LandPlot active = ownedPlot(7L, 11L, LandPlotStatus.ACTIVE);
        when(repository.findByIdAndUserId(11L, 7L)).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> service.updateStatus(7L, 11L, LandPlotStatus.ARCHIVED))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getMessageCode()).isEqualTo("LAND_PLOT_USE_ARCHIVE_ENDPOINT"));
    }

    @Test
    void deactivateRejectsPlotWithOpenSeason() {
        LandPlot active = ownedPlot(7L, 11L, LandPlotStatus.ACTIVE);
        when(repository.findByIdAndUserId(11L, 7L)).thenReturn(Optional.of(active));
        when(cropSeasonRepository.existsByLandPlotIdAndStatusIn(eq(11L), any())).thenReturn(true);

        assertThatThrownBy(() -> service.updateStatus(7L, 11L, LandPlotStatus.INACTIVE))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getMessageCode()).isEqualTo("LAND_PLOT_HAS_OPEN_SEASON"));

        verify(repository, never()).save(any());
    }

    private LandPlotRequest.Create createRequest(String code) {
        return new LandPlotRequest.Create(
                code,
                " North Plot ",
                new BigDecimal("1250.500"),
                AreaUnit.M2,
                "Can Tho",
                new BigDecimal("10.0300000"),
                new BigDecimal("105.7800000"),
                "Alluvial",
                "Canal",
                "Vegetable production"
        );
    }

    private LandPlotRequest.Update updateRequest() {
        return new LandPlotRequest.Update(
                "PLOT-001",
                "Updated Plot",
                new BigDecimal("1500.000"),
                AreaUnit.M2,
                "Can Tho",
                null,
                null,
                null,
                null,
                null
        );
    }

    private LandPlot ownedPlot(Long userId, Long id, LandPlotStatus status) {
        LandPlot entity = new LandPlot();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setPlotCode("PLOT-001");
        entity.setName("North Plot");
        entity.setAreaValue(new BigDecimal("1250.500"));
        entity.setAreaUnit(AreaUnit.M2);
        entity.setStatus(status);
        entity.setCreatedAt(Instant.parse("2026-08-27T10:00:00Z"));
        entity.setUpdatedAt(Instant.parse("2026-08-27T10:00:00Z"));
        return entity;
    }
}

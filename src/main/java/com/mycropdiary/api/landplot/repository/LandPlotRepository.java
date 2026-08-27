package com.mycropdiary.api.landplot.repository;

import com.mycropdiary.api.landplot.entity.LandPlot;
import com.mycropdiary.api.landplot.entity.LandPlotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LandPlotRepository extends JpaRepository<LandPlot, Long> {

    @Query("""
            SELECT p FROM LandPlot p
            WHERE p.userId = :userId
              AND (:status IS NULL OR p.status = :status)
              AND (:keyword IS NULL
                   OR LOWER(p.plotCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(p.address, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(p.soilType, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(p.waterSource, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<LandPlot> searchOwnedPlots(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("status") LandPlotStatus status,
            Pageable pageable
    );

    Optional<LandPlot> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndPlotCodeIgnoreCase(Long userId, String plotCode);

    boolean existsByUserIdAndPlotCodeIgnoreCaseAndIdNot(Long userId, String plotCode, Long id);
}

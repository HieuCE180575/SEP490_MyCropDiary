package com.mycropdiary.api.landplot;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LandPlotRepository extends JpaRepository<LandPlot, Long> {
    Page<LandPlot> findByUserIdAndStatus(Long userId, LandPlot.Status status, Pageable pageable);
    Optional<LandPlot> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndPlotCodeIgnoreCase(Long userId, String plotCode);
}

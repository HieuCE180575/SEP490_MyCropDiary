package com.mycropdiary.api.season.repository;

import com.mycropdiary.api.season.entity.CropSeason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CropSeasonRepository extends JpaRepository<CropSeason, Long> {

    Page<CropSeason> findByUserId(Long userId, Pageable pageable);

    Page<CropSeason> findByUserIdAndArchived(Long userId, boolean archived, Pageable pageable);

    Optional<CropSeason> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT c FROM CropSeason c WHERE c.userId = :userId AND " +
           "(LOWER(c.seasonName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "(c.notes IS NOT NULL AND LOWER(c.notes) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<CropSeason> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);
}

package com.stlghana.admin_service.repository;

import com.stlghana.admin_service.model.SetupModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SetupRepository extends JpaRepository<SetupModel, UUID> {

    default List<SetupModel> findAllByName(String searchValue) {
        return findAllByName(searchValue, Pageable.unpaged()).getContent();
    }


    @Query("""
        SELECT s
        FROM  SetupModel  s
        WHERE lower(s.name)  LIKE lower(concat('%', :search, '%')) 
    """)
    Page<SetupModel> findAllByName(@Param("search") String searchValue, Pageable pageable);


    default List<SetupModel> findAllBySetupCategoryId(List<UUID> setupCategoryIds) {
        return findAllBySetupsCategoryIds(setupCategoryIds,Pageable.unpaged()).getContent();
    }


    @Query("""
        SELECT s
        FROM  SetupModel  s
        WHERE s.setupCategory.id IN (:setupCategoryIds)
    """)
    Page<SetupModel> findAllBySetupsCategoryIds(@Param("setupCategoryIds")
                                                List<UUID> setupCategoryIds, Pageable pageable);
}

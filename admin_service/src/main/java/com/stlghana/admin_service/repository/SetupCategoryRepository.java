package com.stlghana.admin_service.repository;

import com.stlghana.admin_service.model.SetupCategoryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SetupCategoryRepository extends JpaRepository<SetupCategoryModel, UUID> {
        default List<SetupCategoryModel> findAllByName(String searchValue) {
            return findAllByName(searchValue, Pageable.unpaged()).getContent();
        }

        @Query("""
        SELECT sc
        FROM  SetupCategoryModel  sc
        WHERE lower(sc.name)  LIKE lower(concat('%', :search, '%')) 
    """)
        Page<SetupCategoryModel> findAllByName(@Param("search") String searchValue, Pageable pageable);
    }

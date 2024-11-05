package com.stlghana.admin_service.repository;

import com.stlghana.admin_service.model.PermissionCategoryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PermissionCategoryRepository extends JpaRepository<PermissionCategoryModel, UUID> {

        default List<PermissionCategoryModel> findAllByName(String searchValue) {
            return findAllByName(searchValue, Pageable.unpaged()).getContent();
        }


        @Query("""
        SELECT pc
        FROM  PermissionCategoryModel  pc
        WHERE lower(pc.name)  LIKE lower(concat('%', :search, '%')) 
    """)
        Page<PermissionCategoryModel> findAllByName(@Param("search") String searchValue, Pageable pageable);
    }

package com.stlghana.admin_service.repository;

import com.stlghana.admin_service.model.PermissionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<PermissionModel, UUID> {

    @Query("""
            SELECT p 
            FROM PermissionModel p 
            WHERE lower(p.name) LIKE lower(concat('%', :search, '%')) 
    """)
    List<PermissionModel> findAllByName(@Param("search") String searchValue);

    @Query("""
        SELECT r
        FROM  PermissionModel  r
        WHERE lower(r.name)  LIKE lower(concat('%', :search, '%'))
    """)
    Page<PermissionModel> findAllByName(@Param("search") String searchValue, Pageable pageable);

    @Query("""
            SELECT p.name
            FROM PermissionModel p
            WHERE p.id IN (:ids)            
""")
    List<String> findNamesByIds(@Param("ids") List<UUID> permissions);

    @Query("""
            SELECT p.name
            FROM PermissionModel p
            WHERE p.id IN (:ids)            
""")
    Page<String> findNamesByIds(@Param("ids") List<UUID> permissions, Pageable pageable);
}

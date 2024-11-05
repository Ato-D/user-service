package com.stlghana.admin_service.repository;

import com.stlghana.admin_service.model.RoleModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleModel, UUID> {

    @Query("""
        SELECT r
        FROM RoleModel r
        WHERE lower(r.name) LIKE lower(concat('%', :search, '%'))
""")
    List<RoleModel> findAllByName(@Param("search") String searchValue);

    @Query("""
        SELECT r 
        FROM RoleModel r
        WHERE lower(r.name) LIKE lower(concat('%', :search, '%')) 
""")
    Page<RoleModel> findAllByName(@Param("search") String searchValue, Pageable pageable);

    @Query("""  
        SELECT r.name
        FROM RoleModel r
        WHERE r.id IN (:ids)
""")
    List<String> findNamesByIds(@Param("ids") List<UUID> roles);

    @Query("""
        SELECT r.name
        FROM RoleModel r 
        WHERE r.id IN :ids
""")
    List<String> findNameById(@Param("id") UUID id);
}

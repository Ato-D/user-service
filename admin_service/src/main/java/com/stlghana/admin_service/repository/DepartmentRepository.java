package com.stlghana.admin_service.repository;

import com.stlghana.admin_service.model.DepartmentModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<DepartmentModel,UUID> {



    default List<DepartmentModel> findAllByName(String searchValue) {
        return findAllByName(searchValue, Pageable.unpaged()).getContent();
    }


    @Query("""
        SELECT c
        FROM  DepartmentModel  c
        WHERE lower(c.name)  LIKE lower(concat('%', :search, '%')) 
    """)
    Page<DepartmentModel> findAllByName(@Param("search") String searchValue, Pageable pageable);
}

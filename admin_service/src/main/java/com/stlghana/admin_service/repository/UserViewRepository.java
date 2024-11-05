package com.stlghana.admin_service.repository;

import com.stlghana.admin_service.model.views.UserView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserViewRepository extends JpaRepository<UserView, UUID> {

    default List<UserView> findAllUsersByFirstNameOrLastName(String searchValue) {
        return findAllUsersByFirstNameOrLastName(searchValue, Pageable.unpaged()).getContent();
    }

    @Query("""
        SELECT u
        FROM UserView u
        WHERE (lower(u.firstName) LIKE lower(concat('%', :search, '%'))
        OR lower(u.lastName) LIKE lower(concat('%', :search, '%')) )
""")
    Page<UserView> findAllUsersByFirstNameOrLastName(@Param("search") String searchValue, Pageable pageable);


    default List<UserView> findAllUsersByDepartmentAndFirstNameOrLastName(String searchValue, UUID departmentId) {
        return findAllUsersByDepartmentAndFirstNameOrLastName(searchValue,departmentId,Pageable.unpaged()).getContent();
    }

    @Query("""
            SELECT u
            FROM UserView u
            WHERE u.departmentId = :departmentId
            AND (lower(u.firstName) LIKE lower(concat('%', :search, '%'))
            OR lower(u.lastName) LIKE lower(concat('%', :search, '%')) )
""")
    Page<UserView> findAllUsersByDepartmentAndFirstNameOrLastName(@Param("search") String searchValue,
                                                                  @Param("departmentId") UUID departmentId,
                                                                  Pageable pageable);



    default List<UserView> findAllUsersByManagerIdAndFirstNameOrLastName(String searchValue, UUID managerId) {
        return findAllUsersByManagerIdAndFirstNameOrLastName(searchValue,managerId,Pageable.unpaged()).getContent();
    }

    @Query("""
    SELECT u 
    FROM UserView u
    WHERE u.managerId = :managerId
    AND (lower(u.firstName)  LIKE lower(concat('%', :search, '%')) 
    OR lower(u.lastName)  LIKE lower(concat('%', :search, '%')) )
    """)
    Page<UserView> findAllUsersByManagerIdAndFirstNameOrLastName(@Param("search") String searchValue,
                                                                 @Param("managerId") UUID managerId,
                                                                 Pageable pageable);


    default List<UserView> findAllUsersByRoleIdsAndFirstNameOrLastName(String searchValue, UUID[] roleIds) {
        return findAllUsersByRoleIdsAndFirstNameOrLastName(searchValue,roleIds,Pageable.unpaged()).getContent();
    }
    @Query(value = """
    SELECT * 
    FROM user_v u
    WHERE u.roles && :roleIds
    AND (lower(u.first_name) LIKE lower(concat('%', :search, '%')) 
    OR lower(u.last_name) LIKE lower(concat('%', :search, '%')))
    """, nativeQuery = true)
    Page<UserView> findAllUsersByRoleIdsAndFirstNameOrLastName(@Param("search") String searchValue,
                                                               @Param("roleIds") UUID[] roleIds,
                                                               Pageable pageable);



}

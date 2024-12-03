package com.wibeechat.missa.repository;

import com.wibeechat.missa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface UserRepository extends JpaRepository<User, String> {
    @Query(value = """
        SELECT * FROM (
            SELECT u.*, ROWNUM rnum
            FROM wibee.user_info u
            WHERE (:vipFlag IS NULL OR u.USER_STATUS = :vipFlag)
              AND ROWNUM <= :endRow
        ) WHERE rnum > :startRow
        """, nativeQuery = true)
    List<User> findUsersWithPaginationAndVIPFilter(
        @Param("startRow") int startRow,
        @Param("endRow") int endRow,
        @Param("vipFlag") String vipFlag
    );

    @Query(value = "SELECT COUNT(*) FROM wibee.user_info WHERE USER_STATUS = :vipFlag", nativeQuery = true)
    int countUsersWithVIPFilter(@Param("vipFlag") String vipFlag);

    @Query(value = "SELECT COUNT(*) FROM WIBEE.user_info", nativeQuery = true)
    int countAllUsers();
}
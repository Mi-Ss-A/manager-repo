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
                WHERE ROWNUM <= :endRow
            ) WHERE rnum > :startRow
            """, nativeQuery = true)
    List<User> findUsersWithPagination(@Param("startRow") int startRow, @Param("endRow") int endRow);
}
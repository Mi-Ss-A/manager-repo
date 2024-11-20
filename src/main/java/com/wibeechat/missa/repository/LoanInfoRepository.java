package com.wibeechat.missa.repository;

import com.wibeechat.missa.entity.LoanInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanInfoRepository extends JpaRepository<LoanInfo, String> {
    @Query(value = """
            SELECT * FROM (
                SELECT c.*, ROWNUM rnum
                FROM WIBEE.LOAN_INFO c
                WHERE ROWNUM <= :endRow
            ) WHERE rnum > :startRow
            """, nativeQuery = true)
    List<LoanInfo> findLoansWithPagination(@Param("startRow") int startRow, @Param("endRow") int endRow);

    @Query(value = "SELECT COUNT(*) FROM WIBEE.LOAN_INFO", nativeQuery = true)
    int countAllLoans();

}

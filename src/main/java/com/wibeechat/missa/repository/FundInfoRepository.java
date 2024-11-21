package com.wibeechat.missa.repository;

import com.wibeechat.missa.entity.FundInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundInfoRepository extends JpaRepository<FundInfo, String> {
    @Query(value = """
            SELECT * FROM (
                SELECT c.*, ROWNUM rnum
                FROM WIBEE.FUND_MASTER c
                WHERE ROWNUM <= :endRow
            ) WHERE rnum > :startRow
            """, nativeQuery = true)
    List<FundInfo> findFundsWithPagination(@Param("startRow") int startRow, @Param("endRow") int endRow);

    @Query(value = "SELECT COUNT(*) FROM WIBEE.FUND_MASTER", nativeQuery = true)
    int countAllFunds();

}

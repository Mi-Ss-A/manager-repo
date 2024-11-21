package com.wibeechat.missa.repository;

import com.wibeechat.missa.entity.CardInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardInfoRepository extends JpaRepository<CardInfo, String> {

    @Query(value = """
            SELECT * FROM (
                SELECT c.*, ROWNUM rnum
                FROM WIBEE.CARD_INFO c
                WHERE ROWNUM <= :endRow
            ) WHERE rnum > :startRow
            """, nativeQuery = true)
    List<CardInfo> findCardsWithPagination(@Param("startRow") int startRow, @Param("endRow") int endRow);

    @Query(value = "SELECT COUNT(*) FROM WIBEE.CARD_INFO", nativeQuery = true)
    int countAllCards();
}
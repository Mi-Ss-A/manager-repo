package com.wibeechat.missa.service;

import com.wibeechat.missa.entity.FundInfo;
import com.wibeechat.missa.repository.FundInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FundInfoService {
    @Autowired
    private FundInfoRepository fundInfoRepository;

    public List<FundInfo> getFundsWithPagination(int page, int size) {
        int startRow = page * size;
        int endRow = startRow + size;
        return fundInfoRepository.findFundsWithPagination(startRow, endRow);
    }

    public int getTotalFunds() {
        return fundInfoRepository.countAllFunds();

    }
    public void deleteFundById(String fundCode) {
        if (fundInfoRepository.existsById(fundCode)) {
            fundInfoRepository.deleteById(fundCode);
        } else {
            throw new IllegalArgumentException("Fund with CODE " + fundCode + " does not exist.");
        }
    }

    public void addFund(FundInfo fundInfo) {
        fundInfoRepository.save(fundInfo); // prePersist에서 UUID 자동 생성
    }

}

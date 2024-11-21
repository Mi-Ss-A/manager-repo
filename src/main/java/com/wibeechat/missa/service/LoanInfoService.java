package com.wibeechat.missa.service;

import com.wibeechat.missa.entity.LoanInfo;
import com.wibeechat.missa.repository.LoanInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanInfoService {

    @Autowired
    private LoanInfoRepository loanInfoRepository;

    public List<LoanInfo> getLoansWithPagination(int page,int size){
        int startRow = page * size;
        int endRow = startRow + size;
        return loanInfoRepository.findLoansWithPagination(startRow,endRow);
    }

    public int getTotalLoans() {
        return loanInfoRepository.countAllLoans();
    }

    public void deleteLoanById(String loanId) {
        if (loanInfoRepository.existsById(loanId)) {
            loanInfoRepository.deleteById(loanId);
        } else {
            throw new IllegalArgumentException("Loan with ID " + loanId + " does not exist.");
        }
    }

    public void addLoan(LoanInfo loanInfo) {
        loanInfoRepository.save(loanInfo); // prePersist에서 UUID 자동 생성
    }

}




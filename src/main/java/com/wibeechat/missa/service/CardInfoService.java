package com.wibeechat.missa.service;

import com.wibeechat.missa.entity.CardInfo;
import com.wibeechat.missa.entity.LoanInfo;
import com.wibeechat.missa.repository.CardInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardInfoService {

    @Autowired
    private CardInfoRepository cardInfoRepository;

    public List<CardInfo> getCardsWithPagination(int page, int size) {
        int startRow = page * size;
        int endRow = startRow + size;
        return cardInfoRepository.findCardsWithPagination(startRow, endRow);
    }

    public int getTotalCards() {
        return cardInfoRepository.countAllCards();

    }
    public void deleteCardById(String cardCode) {
        if (cardInfoRepository.existsById(cardCode)) {
            cardInfoRepository.deleteById(cardCode);
        } else {
            throw new IllegalArgumentException("Card with CODE " + cardCode + " does not exist.");
        }
    }

    public void addCard(CardInfo cardInfo) {
        cardInfoRepository.save(cardInfo); // prePersist에서 UUID 자동 생성
    }
}

package com.wibeechat.missa.service;

import com.wibeechat.missa.entity.CardInfo;
import com.wibeechat.missa.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    public List<CardInfo> getCardsWithPagination(int page, int size) {
        int startRow = page * size;
        int endRow = startRow + size;
        return cardRepository.findCardsWithPagination(startRow, endRow);
    }

    public int getTotalCards() {
        return cardRepository.countAllCards();
    }
}

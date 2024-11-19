package com.wibeechat.missa.controller;

import com.wibeechat.missa.entity.CardInfo;
import com.wibeechat.missa.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CardController {

    @Autowired
    private CardService cardService;

    @GetMapping("/cards")
    public String getCardList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        try {
            // 카드 정보 페이징 데이터 가져오기
            List<CardInfo> cards = cardService.getCardsWithPagination(page, size);

            // 전체 페이지 수 계산 (총 카드 수는 Service에서 가져오는 것을 권장)
            int totalCards = cardService.getTotalCards(); // 총 카드 수를 가져오는 메서드
            int totalPages = (int) Math.ceil((double) totalCards / size);

            // 모델에 데이터 추가
            model.addAttribute("cards", cards);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);

        } catch (Exception e) {
            // 에러 로그 출력
            System.err.println("Error fetching card list: " + e.getMessage());
            e.printStackTrace();

            // 에러 메시지를 모델에 추가
            model.addAttribute("errorMessage", "An error occurred while loading the card list. Please try again later.");
            model.addAttribute("cards", null); // 카드 데이터를 초기화
        }

        return "card-list"; // Thymeleaf 템플릿 이름
    }

}

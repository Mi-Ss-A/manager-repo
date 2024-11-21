package com.wibeechat.missa.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.wibeechat.missa.entity.CardInfo;
import com.wibeechat.missa.service.CardInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
public class CardController {

    private static final Logger logger = LoggerFactory.getLogger(CardController.class);

    @Autowired
    private CardInfoService cardInfoService;


    @GetMapping("/cards")
    public String getCardList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        try {
            // 카드 정보 페이징 데이터 가져오기
            List<CardInfo> cards = cardInfoService.getCardsWithPagination(page, size);

            // 전체 페이지 수 계산 (총 카드 수는 Service에서 가져오는 것을 권장)
            int totalCards = cardInfoService.getTotalCards(); // 총 카드 수를 가져오는 메서드
            int totalPages = (int) Math.ceil((double) totalCards / size);

            // 모델에 데이터 추가
            model.addAttribute("cards", cards);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize",size);
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

    //Get카드 상품 추가폼
    @GetMapping("/card/add")
    public String showAddCardForm(Model model){
        try{
            model.addAttribute("cardInfo",new CardInfo());
            return "card-add";

        }catch (Exception e){
            System.err.println("Error fetching card add form: " + e.getMessage());
            e.printStackTrace();

            return "error";
        }
    }

    //카드 상품 추가
    @PostMapping("/card/add")
    public String addCard(@ModelAttribute CardInfo cardInfo, RedirectAttributes redirectAttributes){
        try{
            cardInfoService.addCard(cardInfo);
            redirectAttributes.addFlashAttribute("successMessage", "카드 상품이 성공적으로 추가되었습니다!");
        }catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation: {}", e.getMessage(), e);
            // DataIntegrityViolationException 처리
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid card data: " + e.getMessage());
        } catch (Exception e) {
            // 기타 모든 예외 처리
            logger.error("Unexpected error: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding card: " + e.getMessage());
        }
        return "redirect:/cards";
    }

    //카드 상품 삭제
    @PostMapping("/card/delete")
    public String deleteCard(
            @RequestParam(value="cardCode") String cardCode, RedirectAttributes redirectAttributes){
        try{
            cardInfoService.deleteCardById(cardCode);
            redirectAttributes.addFlashAttribute("successMessage", "카드상품이 성공적으로 삭제되었습니다!");
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting card: " + e.getMessage());
        }
        return "redirect:/cards";
    }


}

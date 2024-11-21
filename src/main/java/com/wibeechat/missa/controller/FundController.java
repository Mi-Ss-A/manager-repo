package com.wibeechat.missa.controller;

import com.wibeechat.missa.entity.FundInfo;
import com.wibeechat.missa.service.FundInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class FundController {
    private static final Logger logger = LoggerFactory.getLogger(CardController.class);

    @Autowired
    private FundInfoService fundInfoService;


    @GetMapping("/funds")
    public String getFundsList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        try {
            // 펀드 정보 페이징 데이터 가져오기
            List<FundInfo> funds = fundInfoService.getFundsWithPagination(page, size);

            // 전체 페이지 수 계산 (총 펀드 수는 Service에서 가져오는 것을 권장)
            int totalFunds = fundInfoService.getTotalFunds(); // 총 펀드 수를 가져오는 메서드
            int totalPages = (int) Math.ceil((double) totalFunds / size);

            // 모델에 데이터 추가
            model.addAttribute("funds", funds);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages", totalPages);

        } catch (Exception e) {
            // 에러 로그 출력
            System.err.println("Error fetching fund list: " + e.getMessage());
            e.printStackTrace();

            // 에러 메시지를 모델에 추가
            model.addAttribute("errorMessage", "An error occurred while loading the fund list. Please try again later.");
            model.addAttribute("funds", null); // fund 데이터를 초기화
            return "error";
        }

        return "fund-list";
    }

    //Get펀드상품 추가폼
    @GetMapping("/fund/add")
    public String showAddFundForm(Model model) {
        try {
            model.addAttribute("fundInfo", new FundInfo());
            return "fund-add";

        } catch (Exception e) {
            System.err.println("Error fetching fund add form: " + e.getMessage());
            e.printStackTrace();

            return "error";
        }
    }

    //펀드 상품 추가
    @PostMapping("/fund/add")
    public String addFund(@ModelAttribute FundInfo fundInfo, RedirectAttributes redirectAttributes) {
        try {
            // Debugging: 카테고리 값 확인
            System.out.println("Category: " + fundInfo.getCategory());

            fundInfoService.addFund(fundInfo);
            redirectAttributes.addFlashAttribute("successMessage", "펀드 상품이 성공적으로 추가되었습니다!");
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid fund data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding fund: " + e.getMessage());
        }
        return "redirect:/funds";
    }


    //펀드 상품 삭제
    @PostMapping("/fund/delete")
    public String deleteFund(
            @RequestParam(value="fundCode") String fundCode, RedirectAttributes redirectAttributes){
        try{
            fundInfoService.deleteFundById(fundCode);
            redirectAttributes.addFlashAttribute("successMessage", "펀드상품이 성공적으로 삭제되었습니다!");
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting fund: " + e.getMessage());
        }
        return "redirect:/funds";
    }
}

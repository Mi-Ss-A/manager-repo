package com.wibeechat.missa.controller;

import com.wibeechat.missa.entity.LoanInfo;
import com.wibeechat.missa.service.LoanInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class LoanInfoController {

    @Autowired
    private LoanInfoService loanInfoService;

    @GetMapping("/loans")
    public String getLoanList(
            @RequestParam(value="page",defaultValue = "0") int page,
            @RequestParam(value="size",defaultValue = "10") int size,
            Model model) {

        try{
            //대출 정보 페이징 데이터 가져오기
            List<LoanInfo> loans = loanInfoService.getLoansWithPagination(page, size);

            //전체 페이지 수 계산 (총 대출 상품 수 Service 에서 가져오기)
            int totalLoans = loanInfoService.getTotalLoans(); //총 대출상품 수 가져오기
            int totalPages = (int) Math.ceil((double) totalLoans / size);

            //모델에 데이터 추가
            model.addAttribute("loans",loans);
            model.addAttribute("currentPage",page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages",totalPages);

            return "loan-list";
        }catch(Exception e){
            //에러 로그
            System.err.println("Error fetching card list: " + e.getMessage());
            e.printStackTrace();

            // 에러 메시지를 모델에 추가
            model.addAttribute("errorMessage", "An error occurred while loading the card list. Please try again later.");
            model.addAttribute("loans", null);

            return "error";
        }
    }

    //대출 상품 정보삭제
    @PostMapping("/loans/delete")
    public String deleteLoan(@RequestParam(value = "loanId") String loanId, RedirectAttributes redirectAttributes) {
        try {
            loanInfoService.deleteLoanById(loanId);
            redirectAttributes.addFlashAttribute("successMessage", "Loan deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting loan: " + e.getMessage());
        }
        return "redirect:/loans";
    }

    //대출상품 추가
    @GetMapping("/loans/add")
    public String showAddLoanForm(Model model) {
        model.addAttribute("loanInfo", new LoanInfo()); // 빈 LoanInfo 객체
        return "loan-add"; // 추가 폼 템플릿 이름
    }

    @PostMapping("/loans/add")
    public String addLoan(@ModelAttribute LoanInfo loanInfo, RedirectAttributes redirectAttributes) {
        try {
                loanInfoService.addLoan(loanInfo);
            redirectAttributes.addFlashAttribute("successMessage", "Loan added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding loan: " + e.getMessage());
        }
        return "redirect:/loans";
    }


}

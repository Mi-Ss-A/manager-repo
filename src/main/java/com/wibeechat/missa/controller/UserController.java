package com.wibeechat.missa.controller;

import com.wibeechat.missa.domain.IstioMetrics;
import com.wibeechat.missa.entity.FundInfo;
import com.wibeechat.missa.entity.User;
import com.wibeechat.missa.service.IstioMetricsService;
import com.wibeechat.missa.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);


    @Autowired
    private UserService userService;
    private final IstioMetricsService istioMetricsService;

    private static final List<String> SERVICE_NAMES = Arrays.asList(
            "wibee-user-server-service",
            "wibee-ai-server-service",
            "wibee-config-server-service",
            "wibee-front-end-service"
    );


    @GetMapping("/admin")
    public String dashboard(Model model) {
        try {
            IstioMetrics metrics = istioMetricsService.getIstioMetrics();
            model.addAttribute("metrics", metrics);
            model.addAttribute("SERVICE_NAMES", SERVICE_NAMES);
            model.addAttribute("metricsistio", istioMetricsService.getAllMetrics());
            model.addAttribute("error", null);
        } catch (Exception e) {
            model.addAttribute("metrics", new IstioMetrics());
            model.addAttribute("error", "Failed to fetch Istio metrics: " + e.getMessage());
        }
        return "dashboard";
    }


    @GetMapping("/userlists")
    public String userLists(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        try {

            List<User> users = userService.getUsersWithCustomPagination(page, size);

            //전체 페이지수 계산
            int totalUsers = userService.getTotalUsers(); // 총 유저 수를 가져오는 메서드
            int totalPages = (int) Math.ceil((double) totalUsers / size);

            model.addAttribute("users", users);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
            model.addAttribute("totalPages",totalPages);
        } catch (Exception e) {
            // 에러 로그 출력
            System.err.println("Error fetching user list: " + e.getMessage());
            e.printStackTrace();

            // 에러 메시지를 모델에 추가
            model.addAttribute("errorMessage", "An error occurred while loading the user list. Please try again later.");
            model.addAttribute("users", null); // user 데이터를 초기화
            return "error";
        }

        return "user-list"; // 템플릿 파일 이름
    }

    //Get user 추가폼
    @GetMapping("/user/add")
    public String showAddUserForm(Model model) {
        try {
            model.addAttribute("user", new User());
            return "user-add";

        } catch (Exception e) {
            System.err.println("Error fetching fund add form: " + e.getMessage());
            e.printStackTrace();

            return "error";
        }
    }

    //유저 추가
    @PostMapping("/user/add")
    public String addUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
//            // Debugging: 카테고리 값 확인
//            System.out.println("User: " + user.getUserName());

            userService.addUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "유저가 성공적으로 추가되었습니다!");
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid user data: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding user: " + e.getMessage());
        }
        return "redirect:/userlists";
    }


    //유저 삭제
    @PostMapping("/user/delete")
    public String deleteUser(
            @RequestParam(value="userNo") String userNo, RedirectAttributes redirectAttributes){
        try{
            userService.deleteUserById(userNo);
            redirectAttributes.addFlashAttribute("successMessage", "유저가 성공적으로 삭제되었습니다!");
        }catch(Exception e){
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/userlists";
    }

    // 전역 예외 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(Model model, IllegalArgumentException e) {
        model.addAttribute("errorMessage", "Invalid request parameters: " + e.getMessage());
        return "error"; // 에러 페이지
    }




}


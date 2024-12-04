package com.wibeechat.missa.controller;

import java.security.Principal;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wibeechat.missa.domain.IstioMetrics;
import com.wibeechat.missa.entity.User;
import com.wibeechat.missa.model.UsageData;
import com.wibeechat.missa.service.IstioMetricsService;
import com.wibeechat.missa.service.OpenAIService;
import com.wibeechat.missa.service.UserService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final OpenAIService openAIService;
    private final ObjectMapper objectMapper;
    private final IstioMetricsService istioMetricsService;
    private final UserService userService;




    public static final List<String> SERVICE_NAMES = Arrays.asList(
            "wibee-user-server",
            "wibee-ai-server",
            "wibee-config-server",
            "wibee-front-end",
            "wibee-portfolio-server"
    );


    @GetMapping("/admin")
    public String adminPage(Model model, Principal principal) {
        try {
            // OpenAI 사용량 데이터
            List<UsageData> weeklyUsageData = openAIService.getWeeklyUsageData();
            if (weeklyUsageData == null) {
                weeklyUsageData = new ArrayList<>();
            }

            // 데이터 로깅
            for (UsageData data : weeklyUsageData) {
                if (data != null && data.getData() != null) {
                    logger.info("Date: {}, Items: {}", data.getDate(), data.getData().size());
                }
            }

            model.addAttribute("weeklyUsageData", weeklyUsageData);

            // Istio 메트릭
            IstioMetrics metrics = istioMetricsService.getIstioMetrics();
            if (metrics == null) {
                metrics = new IstioMetrics();
            }
            model.addAttribute("metrics", metrics);

            // 서비스 이름 목록
            model.addAttribute("SERVICE_NAMES", SERVICE_NAMES);

            // Istio 세부 메트릭 초기화
            Map<String, Object> metricsistio = istioMetricsService.getAllMetrics();
            if (metricsistio == null) {
                metricsistio = new HashMap<>();
                metricsistio.put("peakHours", new HashMap<>());
                metricsistio.put("peakTraffic", new HashMap<>());
                metricsistio.put("hourlyTraffic", new HashMap<>());
            }
            model.addAttribute("metricsistio", metricsistio);
            model.addAttribute("error", null);

            // 현재 사용자 이름
            model.addAttribute("username", principal.getName());
        } catch (Exception e) {
            e.printStackTrace(); // 예외 출력
            model.addAttribute("weeklyUsageData", new ArrayList<>()); //빈 객체 전달
            model.addAttribute("metrics", new IstioMetrics());
            model.addAttribute("metricsistio", new HashMap<>());
            model.addAttribute("error", "Failed to fetch data: " + e.getMessage());
        }
        return "dashboard";
    }


@GetMapping("/userlists")
public String userLists(
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
        @RequestParam(value = "vipOnly", defaultValue = "false") boolean vipOnly,
        Model model) {

    try {
        // VIP 필터 적용된 유저 리스트 가져오기
        List<User> users = userService.getUsersWithCustomPagination(page, size, vipOnly);

        // VIP 필터 적용된 전체 유저 수 가져오기
        int totalUsers = userService.getTotalUsers(vipOnly);
        int totalPages = (int) Math.ceil((double) totalUsers / size);

        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("vipOnly", vipOnly); // 현재 VIP 상태 추가
    } catch (Exception e) {
        // 에러 처리
        System.err.println("Error fetching user list: " + e.getMessage());
        e.printStackTrace();
        model.addAttribute("errorMessage", "An error occurred while loading the user list. Please try again later.");
        model.addAttribute("users", null);
        return "error";
    }

    return "user-list";
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


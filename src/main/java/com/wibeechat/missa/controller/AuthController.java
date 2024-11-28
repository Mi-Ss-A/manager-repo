package com.wibeechat.missa.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wibeechat.missa.entity.Administrator;
import com.wibeechat.missa.service.AdministratorService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AdministratorService adminService;

        @GetMapping("/login")
        public String showLoginForm(
                @RequestParam(value = "error", required = false) String error,
                @RequestParam(value = "logout", required = false) String logout,
                Model model) {

            if (error != null) {
                model.addAttribute("errorMessage", "Invalid username or password!");
            }

            if (logout != null) {
                model.addAttribute("logoutMessage", "You have been logged out successfully.");
            }

            return "login";
        }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }

    @GetMapping("/signup")
    public String signupForm(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", error); // 에러 메시지를 모델에 추가
        }
        return "signup"; // signup.html 렌더링
    }

    @PostMapping("/signup")
    public String processSignup(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        // 비밀번호 일치 여부 확인
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match!"); // 에러 메시지 추가
            return "signup"; // 다시 signup 페이지로 이동
        }

        // 이미 존재하는 사용자 확인
        Optional<Administrator> existingAdmin = adminService.findByUsername(username);
        if (existingAdmin.isPresent()) {
            model.addAttribute("errorMessage", "Username already exists!"); // 에러 메시지 추가
            return "signup"; // 다시 signup 페이지로 이동
        }

        // 사용자 등록
        try {
            adminService.createAdministrator(username, password, "Super Admin");
            return "redirect:/login"; // 회원가입 성공 후 로그인 페이지로 리다이렉트
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred during registration."); // 일반적인 에러 메시지
            return "signup";
        }
    }
}

package com.wibeechat.missa.controller;

import com.wibeechat.missa.entity.Administrator;
import com.wibeechat.missa.service.AdministratorService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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

    @PostMapping("/login")
    public String login(@RequestParam(value="username") String username,
                        @RequestParam(value="password") String password,
                        HttpSession session,
                        Model model) {
        Optional<Administrator> optionalAdmin = adminService.findByUsername(username);
        if (optionalAdmin.isPresent()) {
            Administrator admin = optionalAdmin.get();
            // 비밀번호를 해싱하지 않고 직접 비교
            if (password.equals(admin.getPasswordHash())) { // passwordHash 필드가 평문 비밀번호를 저장한다고 가정
                // 로그인 성공, 세션에 사용자 정보 저장
                session.setAttribute("admin", admin);
                return "redirect:/admin";
            }
        }
        // 로그인 실패
        model.addAttribute("errorMessage", "Invalid username or password.");
        return "login";
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}

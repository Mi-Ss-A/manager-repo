package com.wibeechat.missa.controller;

import com.wibeechat.missa.entity.User;
import com.wibeechat.missa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Controller
public class AppAdminController {

    @Autowired
    private UserService userService;


    @GetMapping("/admin")
    public String adminPage(Model model) {
        return "appAdminHome";
    }

    @GetMapping("/userlists")
    public String userLists(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {

        try {
            List<User> users = userService.getUsersWithCustomPagination(page, size);
            model.addAttribute("users", users);
            model.addAttribute("currentPage", page);
            model.addAttribute("pageSize", size);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading user list: " + e.getMessage());
        }

        return "user-list"; // 템플릿 파일 이름
    }


    // 전역 예외 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(Model model, IllegalArgumentException e) {
        model.addAttribute("errorMessage", "Invalid request parameters: " + e.getMessage());
        return "error"; // 에러 페이지
    }




}


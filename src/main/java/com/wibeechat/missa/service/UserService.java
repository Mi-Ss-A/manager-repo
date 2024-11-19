package com.wibeechat.missa.service;

import com.wibeechat.missa.entity.User;
import com.wibeechat.missa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getUsersWithCustomPagination(int page, int size) {
        int startRow = page * size;
        int endRow = startRow + size;
        return userRepository.findUsersWithPagination(startRow, endRow);
    }
}

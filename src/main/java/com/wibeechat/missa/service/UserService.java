package com.wibeechat.missa.service;

import com.wibeechat.missa.entity.FundInfo;
import com.wibeechat.missa.entity.User;
import com.wibeechat.missa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getUsersWithCustomPagination(int page, int size, boolean vipOnly) {
        int startRow = page * size;
        int endRow = startRow + size;
        
        String vipFlag = vipOnly == true ? "V" : null;
        return userRepository.findUsersWithPaginationAndVIPFilter(startRow, endRow, vipFlag);
    }
    
    public int getTotalUsers(boolean vipOnly) {
        if (vipOnly) {
            return userRepository.countUsersWithVIPFilter("V");
        } else {
            return userRepository.countAllUsers();
        }
    }

    public int getTotalUsers() {
        return userRepository.countAllUsers();

    }
    public void deleteUserById(String userNo) {
        if (userRepository.existsById(userNo)) {
            userRepository.deleteById(userNo);
        } else {
            throw new IllegalArgumentException("user with NUMBER ->  " + userNo + " does not exist.");
        }
    }

    public void addUser(User user) {
        userRepository.save(user); // prePersist에서 UUID 자동 생성
    }
}

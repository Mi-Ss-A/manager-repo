package com.wibeechat.missa.service;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wibeechat.missa.entity.Administrator;
import com.wibeechat.missa.entity.Role;
import com.wibeechat.missa.repository.AdministratorRepository;
import com.wibeechat.missa.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdministratorService {

    @Autowired
    private AdministratorRepository adminRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public Administrator createAdministrator(String username, String rawPassword, String roleName) {
        Role role = roleRepo.findByRoleName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        Administrator admin = Administrator.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();

        return adminRepo.save(admin);
    }

    public Optional<Administrator> findByUsername(String username) {
        return adminRepo.findByUsername(username);
    }
}

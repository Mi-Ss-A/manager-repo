package com.wibeechat.missa.service;

import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.wibeechat.missa.entity.Administrator;
import com.wibeechat.missa.repository.AdministratorRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AdministratorRepository administratorRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Administrator admin = administratorRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(admin);
    }

    private static class User implements UserDetails {

        private final Administrator admin;

        public User(Administrator admin) {
            this.admin = admin;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singleton(new SimpleGrantedAuthority(admin.getRole().getRoleName()));
        }

        @Override
        public String getPassword() {
            return admin.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return admin.getUsername();
        }
    }
}


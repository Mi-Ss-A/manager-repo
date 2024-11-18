package com.wibeechat.missa.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String message;
    private AlertLevel level;
    private LocalDateTime timestamp;
    private boolean acknowledged;

    public enum AlertLevel {
        INFO, WARNING, ERROR
    }
}
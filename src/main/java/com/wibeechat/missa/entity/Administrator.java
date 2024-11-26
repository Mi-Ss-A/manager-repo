package com.wibeechat.missa.entity;


import com.wibeechat.missa.converter.UUIDConverter;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ADMINISTRATORS")
public class Administrator {

    @Id
    @Column(name = "ID", columnDefinition = "RAW(16)")
    @Convert(converter = UUIDConverter.class)
    private UUID id;

    @Column(name = "USERNAME", nullable = false, unique = true)
    private String username;

    @Column(name = "PASSWORD_HASH", nullable = false)
    private String passwordHash;

    @ManyToOne
    @JoinColumn(name = "ROLE", nullable = false)
    private Role role;


    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

}


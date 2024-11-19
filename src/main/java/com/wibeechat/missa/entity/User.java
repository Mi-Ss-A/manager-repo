package com.wibeechat.missa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(schema = "WIBEE", name = "USER_INFO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "USER_NO", length = 100, nullable = false)
    private String userNo;

    @Column(name = "USER_ID", length = 20, nullable = false)
    private String userId;

    @Column(name = "USER_PW", length = 100, nullable = false)
    private String userPw;

    @Column(name = "USER_PHONE_NUMBER", length = 15, nullable = false)
    private String userPhoneNumber;

    @Column(name = "USER_NAME", length = 20, nullable = false)
    private String userName;

    @Column(name = "USER_EMAIL", length = 100, nullable = false)
    private String userEmail;

    @Column(name = "USER_DATE_OF_BIRTH", nullable = false)
    private LocalDate userDateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "USER_GENDER", length = 1, nullable = false)
    private Gender userGender;

    @Column(name = "USER_ADDRESS", length = 200, nullable = false)
    private String userAddress;

    @Column(name = "USER_REGISTRATION_DATE")
    private LocalDateTime userRegistrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "USER_STATUS", length = 1)
    private UserStatus userStatus;

    @Column(name = "USER_TYPE", length = 5, nullable = false)
    private String userType;

    @PrePersist
    public void prePersist() {
        if (this.userStatus == null) {
            this.userStatus = UserStatus.A; // 기본값 설정
        }
        if (this.userRegistrationDate == null) {
            this.userRegistrationDate = LocalDateTime.now(); // 기본값 설정
        }
    }
}

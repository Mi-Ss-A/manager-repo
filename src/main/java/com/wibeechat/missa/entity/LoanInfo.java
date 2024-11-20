package com.wibeechat.missa.entity;


import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "LOAN_INFO", schema = "WIBEE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanInfo {

    @Id
    @Column(name = "LOAN_ID", nullable = false, length = 100)
    private String loanId; // UUID를 문자열로 저장

    @Column(name = "LOAN_TYPE", nullable = false, length = 30)
    private String loanType;

    @Column(name = "LOAN_NAME", nullable = false, length = 100)
    private String loanName;

    @Column(name = "LOAN_TARGET", nullable = false, length = 1000)
    private String loanTarget;

    @Column(name = "LOAN_PERIOD", length = 500)
    private String loanPeriod;

    @Column(name = "LOAN_LIMIT", nullable = false, length = 500)
    private String loanLimit;

    @PrePersist
    public void prePersist() {
        if (this.loanId == null || this.loanId.isEmpty()) {
            this.loanId = UUID.randomUUID().toString(); // UUID 자동 생성
        }
    }
}

//package com.wibeechat.missa.entity;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//
//@Entity
//@Table(name = "bank_account_info")
//@Getter
//@Setter
//@NoArgsConstructor
//public class BankAccountInfo {
//
//    @Id
//    @Column(name = "bank_account_id", length = 100, nullable = false)
//    private String bankAccountId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_no", nullable = false)
//    private User userInfo;
//
//    @Column(name = "bank_account_number", length = 20, nullable = false)
//    private String bankAccountNumber;
//
//    @Column(name = "bank_account_type", length = 5, nullable = false)
//    private String bankAccountType;
//
//    @Column(name = "bank_account_status", length = 1, nullable = false)
//    private String bankAccountStatus;
//
//    @Column(name = "bank_account_balance", nullable = false)
//    private BigDecimal bankAccountBalance;
//
//    @Column(name = "bank_account_local_code", length = 5, nullable = false)
//    private String bankAccountLocalCode;
//
//    @Column(name = "bank_account_issue_date", nullable = false)
//    private LocalDate bankAccountIssueDate;
//
//    @Column(name = "bank_account_maturity_date", nullable = false)
//    private LocalDate bankAccountMaturityDate;
//
//    @Column(name = "bank_account_product_name", length = 50, nullable = false)
//    private String bankAccountProductName;
//
//    @Column(name = "bank_accoount_last_transaction", nullable = false)
//    private LocalDate bankAccountLastTransaction;
//}
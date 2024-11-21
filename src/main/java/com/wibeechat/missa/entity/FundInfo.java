package com.wibeechat.missa.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "FUND_MASTER", schema = "WIBEE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundInfo {
    @Id
    @Column(name = "FUND_CODE", nullable = false, length = 100)
    private String fundCode;

    @Column(name = "FUND_NAME", nullable = false, length = 300)
    private String fundName;

    @Column(name = "CATEGORY", nullable = false, length = 5)
    private String category;

    @Column(name = "FUND_TYPE", nullable = false, length = 5)
    private String fundType;

    @PrePersist
    public void prePersist(){
        if(this.fundCode == null || this.fundCode.isEmpty()){
            this.fundCode = UUID.randomUUID().toString();
        }
    }
}

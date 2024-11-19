package com.wibeechat.missa.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CARD_INFO", schema = "WIBEE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardInfo {

    @Id
    @Column(name = "CARD_CODE", nullable = false, length = 100)
    private String cardCode;

    @Column(name = "CARD_TYPE", nullable = false, length = 5)
    private String cardType;

    @Column(name = "CARD_ANNUAL_FEE")
    private Double cardAnnualFee;

    @Column(name = "CARD_BENEFIT", length = 1000)
    private String cardBenefit;

    @Column(name = "CARD_NAME", nullable = false, length = 100)
    private String cardName;

    @Column(name = "CARD_IMG_URL", length = 150)
    private String cardImgUrl;
}

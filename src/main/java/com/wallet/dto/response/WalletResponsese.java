package com.wallet.dto.response;


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponsese {

    private Long walletId;

    private BigDecimal balance;
}
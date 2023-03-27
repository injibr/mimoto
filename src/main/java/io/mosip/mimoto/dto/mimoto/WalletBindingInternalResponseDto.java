package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class WalletBindingInternalResponseDto {
    private String certificate;
    private String walletUserId;
    private String expireDateTime;
}

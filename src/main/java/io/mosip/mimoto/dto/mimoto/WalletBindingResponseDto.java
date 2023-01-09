package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class WalletBindingResponseDto {
    private String certificate;
    private String encryptedWalletBindingId;
    private String expireDateTime;
}

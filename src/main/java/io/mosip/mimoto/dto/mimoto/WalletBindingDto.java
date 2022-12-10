package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class WalletBindingDto {
    private String individualId;
    private String otp;
    private String transactionID;
    private String publicKey;
}

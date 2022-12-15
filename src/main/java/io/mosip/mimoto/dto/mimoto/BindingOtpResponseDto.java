package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class BindingOtpResponseDto {
    private String transactionId;
    private String maskedEmail;
    private String maskedMobile;
}

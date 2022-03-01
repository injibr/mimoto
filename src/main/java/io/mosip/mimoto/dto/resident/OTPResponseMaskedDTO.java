package io.mosip.mimoto.dto.resident;

import lombok.Data;

@Data
public class OTPResponseMaskedDTO {

    private String maskedMobile;
    private String maskedEmail;

}

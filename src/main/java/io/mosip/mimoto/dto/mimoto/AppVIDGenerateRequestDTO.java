package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class AppVIDGenerateRequestDTO {
    private String individualId;
    private String individualIdType;
    private String otp;
    private String vidType;
    private String transactionID;
}
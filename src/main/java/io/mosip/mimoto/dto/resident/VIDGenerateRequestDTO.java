package io.mosip.mimoto.dto.resident;

import lombok.Data;

@Data
public class VIDGenerateRequestDTO {
    private String vidType;
    private String individualIdType;
    private String individualId;
    private String otp;
    private String transactionID;
}

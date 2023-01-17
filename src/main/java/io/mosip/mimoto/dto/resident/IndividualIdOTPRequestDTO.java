package io.mosip.mimoto.dto.resident;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class IndividualIdOTPRequestDTO {

    private String id;
    private String version;
    private String transactionId;
    private String requestTime;
    private List<String> otpChannel;
    private String individualId;
    private Map<String, Object> metadata;

}
package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

import java.util.List;

@Data
public class IndividualIdOtpRequestDTO {
    private String aid;
    private List<String> otpChannel;
    private String transactionID;
}
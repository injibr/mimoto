package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class IdpAuthenticateRequestDto {
    private String requesttime;
    private String linkTransactionId;
    private String individualId;
    private String authFactorType;
    private String challenge;
}

package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class IdpAuthAndConsentDto {
    private String requestTime;
    private AuthAndConsentRequestDto request;
}

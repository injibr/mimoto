package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class IdpConsentDto {
    private String requestTime;
    private IdpConsentRequestDto request;
}

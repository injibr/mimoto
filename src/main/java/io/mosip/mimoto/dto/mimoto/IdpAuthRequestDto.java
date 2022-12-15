package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class IdpAuthRequestDto {
    private String requestTime;
    private IdpAuthInternalRequestDto request;
}

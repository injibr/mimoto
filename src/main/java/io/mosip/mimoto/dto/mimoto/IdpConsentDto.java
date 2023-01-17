package io.mosip.mimoto.dto.mimoto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdpConsentDto {
    private String requestTime;
    private IdpConsentRequestDto request;
}

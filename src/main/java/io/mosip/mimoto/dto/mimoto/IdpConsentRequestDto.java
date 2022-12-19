package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

import java.util.List;

@Data
public class IdpConsentRequestDto {
    private String linkedTransactionId;
    private List<String> acceptedClaims;
    private List<String> permittedAuthorizeScopes;
}

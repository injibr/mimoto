package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

import java.util.List;

@Data
public class AuthAndConsentRequestDto {
    private String linkedTransactionId;
    private String individualId;
    private List<String> acceptedClaims;
    private List<String> permittedAuthorizeScopes;
    private List<IdpAuthChallangeDto> challengeList;
}

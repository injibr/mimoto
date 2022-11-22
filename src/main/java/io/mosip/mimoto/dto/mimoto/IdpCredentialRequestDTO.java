package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class IdpCredentialRequestDTO {
    private String individualId;
    private String otp;
    private String transactionID;
    private String issuer;
    private String credentialType = "vercred";
    private String user;
    private String publicKey;
}

package io.mosip.mimoto.dto.resident;

import lombok.Data;

@Data
public class CredentialRequestStatusResponseDTO {
    private String id;
    private String requestId;
    private String statusCode;
}

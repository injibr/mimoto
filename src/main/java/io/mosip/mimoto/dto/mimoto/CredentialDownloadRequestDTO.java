package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class CredentialDownloadRequestDTO {
    private String individualId;
    private String requestId;
}

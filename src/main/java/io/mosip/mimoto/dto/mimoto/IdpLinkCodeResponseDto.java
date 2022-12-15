package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class IdpLinkCodeResponseDto {
    private String transactionId;
    private String linkCode;
    private String expireDateTime;
}

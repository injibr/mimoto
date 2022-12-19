package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class IdpOtpReq {

    private String requestTime;
    private IdpOtpReqDto request;
}

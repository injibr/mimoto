package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class IdpAuthChallangeDto {
    private String authFactorType;
    private String challenge;
}

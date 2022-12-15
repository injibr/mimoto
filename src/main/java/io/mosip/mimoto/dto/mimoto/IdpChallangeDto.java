package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class IdpChallangeDto {
    private String authFactorType;
    private String challenge;
}

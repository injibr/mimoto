package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

import java.util.List;

@Data
public class IdpAuthInternalRequestDto {
    private String linkedTransactionId;
    private String individualId;
    private List<IdpAuthChallangeDto> challengeList;
}

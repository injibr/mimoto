package io.mosip.mimoto.dto.mimoto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdpAuthInternalRequestDto {
    private String linkedTransactionId;
    private String individualId;
    private List<IdpAuthChallangeDto> challengeList;
}

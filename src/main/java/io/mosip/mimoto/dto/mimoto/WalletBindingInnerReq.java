package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

import java.util.List;

@Data
public class WalletBindingInnerReq {
    private String individualId;
    private List<IdpChallangeDto> challengeList;
    private String publicKey;
    private String authFactorType;
    private String format;
}

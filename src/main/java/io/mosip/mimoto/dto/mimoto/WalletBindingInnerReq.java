package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

import java.util.List;

@Data
public class WalletBindingInnerReq {
    private String transactionId;
    private String individualId;
    private List<IdpChallangeDto> challengeList;
    private String publicKey;
}

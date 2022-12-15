package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

@Data
public class WalletBindingRequestDTO {
    private String requestTime;
    private WalletBindingInnerReq request;

}

package io.mosip.mimoto.dto.mimoto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletBindingInternalRequestDTO {
    private String requestTime;
    private WalletBindingInnerRequestDto request;

}

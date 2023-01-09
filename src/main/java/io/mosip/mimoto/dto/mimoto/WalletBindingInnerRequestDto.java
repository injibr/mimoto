package io.mosip.mimoto.dto.mimoto;

import com.nimbusds.jose.jwk.JWK;
import lombok.Data;

import java.util.List;

@Data
public class WalletBindingInnerRequestDto {
    private String individualId;
    private List<IdpChallangeDto> challengeList;
    private JwkDto publicKey;
    private String authFactorType;
    private String format;
}

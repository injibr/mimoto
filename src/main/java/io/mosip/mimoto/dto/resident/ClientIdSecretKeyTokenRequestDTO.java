package io.mosip.mimoto.dto.resident;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class ClientIdSecretKeyTokenRequestDTO {
    public String clientId;
    public String secretKey;
    public String appId;
}

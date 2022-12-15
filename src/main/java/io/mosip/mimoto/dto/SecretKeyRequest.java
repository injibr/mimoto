package io.mosip.mimoto.dto;

import lombok.*;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SecretKeyRequest {
    public String clientId;
    public String secretKey;
    public String appId;
}

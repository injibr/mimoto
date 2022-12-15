package io.mosip.mimoto.dto.mimoto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwkDto {
    private String kty;
    private String e;
    private String n;
}

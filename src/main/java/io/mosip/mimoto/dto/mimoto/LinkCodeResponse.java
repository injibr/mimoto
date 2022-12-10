package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LinkCodeResponse {
    private String linkTransactionId;
    private String clientName;
    private String logoUrl;
    private List<List<AuthFactorDto>> authFactors;
    private List<String> authorizeScopes;
    private List<String> essentialClaims;
    private List<String> voluntaryClaims;
    private Map<String, Object> configs;
}

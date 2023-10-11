package io.mosip.mimoto.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.Expose;
import lombok.Data;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;


@Data
public class IssuerDTO {
    @Expose
    String id;
    @Expose
    String displayName;
    @Expose
    String protocol;
    @Expose
    String logoUrl;
    @Expose
    String clientId;
    @Expose
    String wellKnownEndpoint;
    @JsonInclude(NON_NULL)
    String redirectUrl;
    @JsonInclude(NON_NULL)
    List<String> scopes;
    @JsonInclude(NON_NULL)
    ServiceConfiguration serviceConfiguration;
    @JsonInclude(NON_NULL)
    Map<String, String> additionalHeaders;
}

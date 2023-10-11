package io.mosip.mimoto.service;

import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public interface IdpService {
    public HttpEntity<MultiValueMap<String, String>> constructGetTokenRequest(Map<String, String> params);
}

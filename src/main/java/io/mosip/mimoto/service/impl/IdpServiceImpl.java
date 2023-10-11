package io.mosip.mimoto.service.impl;

import io.mosip.mimoto.service.IdpService;
import io.mosip.mimoto.util.JoseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Service
public class IdpServiceImpl implements IdpService {

    @Value("${mosip.oidc.client.id}")
    String clientId;

    @Value("${mosip.oidc.client.assertion.type}")
    String clientAssertionType;

    @Autowired
    JoseUtil joseUtil;

    @Override
    public HttpEntity<MultiValueMap<String, String>> constructGetTokenRequest(Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("code", params.get("code"));
        map.add("client_id", clientId);
        map.add("grant_type", params.get("grant_type"));
        map.add("redirect_uri", params.get("redirect_uri"));
        map.add("client_assertion", joseUtil.getJWT(clientId).replace("[","").replace("]",""));
        map.add("client_assertion_type", clientAssertionType);
        map.add("code_verifier", params.get("code_verifier"));

        return new HttpEntity<>(map, headers);
    }
}

package io.mosip.mimoto.util;

import com.google.gson.Gson;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.mimoto.core.http.RequestWrapper;
import io.mosip.mimoto.dto.SecretKeyRequest;
import io.mosip.mimoto.exception.TokenGenerationFailedException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;

/**
 * The Class RestApiClient.
 *
 * @author Rishabh Keshari
 */
@Component
public class RestApiClient {

    /**
     * The logger.
     */
    private Logger logger = LoggerUtil.getLogger(RestApiClient.class);
    private static final String authorization = "Authorization=";
    private static final String RETRIED = "retry";
    private static final String YES = "yes";
    private static final String NO = "no";
    /**
     * The builder.
     */
    @Autowired
    @Qualifier("selfTokenRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier("plainRestTemplate")
    private RestTemplate plainRestTemplate;

    @Value("${mosip.authmanager.client-token-endpoint}")
    private String authBaseUrl;

    @Value("${mosip.iam.adapter.clientid}")
    private String clientId;

    @Value("${mosip.iam.adapter.clientsecret}")
    private String secret;

    @Value("${mosip.iam.adapter.appid}")
    private String appId;

    @Autowired
    Environment environment;

    /**
     * HTTP GET API
     *
     * @param <T>
     * @param uri
     * @param responseType
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public <T> T getApi(URI uri, Class<?> responseType) throws Exception {
        T result = null;
        try {
            logger.info("RestApiClient::getApi()::entry uri: {}", uri);
            result = (T) restTemplate.exchange(uri, HttpMethod.GET, setRequestHeader(null, null), responseType)
                    .getBody();
        } catch (Exception e) {
            logger.error("RestApiClient::getApi()::error uri: {} {} {}", uri, e.getMessage(), e);
        }
        return result;
    }

    /**
     * HTTP GET API
     *
     * @param <T>
     * @param url
     * @param responseType
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getApi(String url, Class<?> responseType) {
        T result = null;
        try {
            result = (T) restTemplate.getForObject(url, responseType);
        } catch (Exception e) {
            logger.error("RestApiClient::getApi()::error uri:{} {} {}", url, e.getMessage(), e);
        }
        return result;
    }

    /**
     * HTTP POST API
     *
     * @param <T>
     * @param uri
     * @param mediaType
     * @param requestType
     * @param responseClass
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public <T> T postApi(String uri, MediaType mediaType, Object requestType, Class<?> responseClass) throws Exception {
        T result = null;
        try {
            logger.info("RestApiClient::postApi()::entry uri: {}", uri);
            result = (T) restTemplate.postForObject(uri, setRequestHeader(requestType, mediaType), responseClass);
        } catch (Exception e) {
            logger.error("RestApiClient::postApi()::error uri: {} {} {}", uri, e.getMessage(), e);
        }
        return result;
    }

    public <T> T postApi(String uri, MediaType mediaType, Object requestType, Class<?> responseClass, boolean useBearerToken) throws Exception {
        T result = null;
        try {
            logger.info("RestApiClient::postApi()::entry uri: {}", uri);
            result = (T) plainRestTemplate.postForObject(uri, setRequestHeader(requestType, mediaType, useBearerToken), responseClass);
        } catch (Exception e) {
            logger.error("RestApiClient::postApi()::error uri: {} {} {}", uri, e.getMessage(), e);
            if (e instanceof HttpClientErrorException) {
                HttpClientErrorException ex = (HttpClientErrorException)e;
                if (ex.getStatusCode().value() == 401) {
                    System.setProperty("token", "");
                    // bearer token renew logic
                    if (System.getProperty(RETRIED) == null || System.getProperty(RETRIED).equalsIgnoreCase(NO)) {
                        logger.info("System will to renew the auth token and retry once more.");
                        System.setProperty(RETRIED, YES);
                        postApi(uri, mediaType, requestType, responseClass, useBearerToken);
                    } else
                        System.setProperty(RETRIED, NO);
                }
            }
        }
        return result;
    }

    private HttpEntity<Object> setRequestHeader(Object requestType, MediaType mediaType) throws IOException {
        return setRequestHeader(requestType, mediaType, false);
    }

    /**
     * this method sets token to header of the request
     *
     * @param requestType
     * @param mediaType
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private HttpEntity<Object> setRequestHeader(Object requestType, MediaType mediaType, boolean useBearerToken) throws IOException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        if (mediaType != null) {
            headers.add("Content-Type", mediaType.toString());
        }

        if (useBearerToken) {
            String bearerToken = System.getProperty("token");
            if (StringUtils.isEmpty(bearerToken))
                bearerToken = getBearerToken();
            headers.add("Authorization", bearerToken);
        }

        if (requestType != null) {
            try {
                HttpEntity<Object> httpEntity = (HttpEntity<Object>) requestType;
                HttpHeaders httpHeader = httpEntity.getHeaders();
                Iterator<String> iterator = httpHeader.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    if (!(headers.containsKey("Content-Type") && key.equals("Content-Type")))
                        headers.add(key, httpHeader.get(key).get(0));
                }
                return new HttpEntity<Object>(httpEntity.getBody(), headers);
            } catch (ClassCastException | NullPointerException e) {
                return new HttpEntity<Object>(requestType, headers);
            }
        } else
            return new HttpEntity<Object>(headers);
    }

    private String getBearerToken() throws IOException {

        SecretKeyRequest request = new SecretKeyRequest(clientId, secret, appId);
        RequestWrapper<SecretKeyRequest> req = new RequestWrapper<>();
        req.setRequest(request);

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(authBaseUrl);
        Gson gson = new Gson();

        StringEntity postingString = new StringEntity(gson.toJson(req));
        post.setEntity(postingString);
        post.setHeader("Content-type", "application/json");
        HttpResponse response = httpClient.execute(post);
        org.apache.http.HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity, "UTF-8");
        Header[] cookie = response.getHeaders("Set-Cookie");
        if (cookie.length == 0)
            throw new TokenGenerationFailedException();
        String token = response.getHeaders("Set-Cookie")[0].getValue();
        token = token.replace(authorization, "");
        token = "Bearer " + token.substring(0, token.indexOf(';'));
        System.setProperty("token", token);
        return token;
    }
}

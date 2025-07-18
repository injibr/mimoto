package io.mosip.mimoto.govbr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.mimoto.dto.idp.TokenResponseDTO;
import io.mosip.mimoto.exception.IdpException;
import io.mosip.mimoto.govbr.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class GovBRServiceImpl implements GovBRService {
    private static final Logger logger = LoggerFactory.getLogger(GovBRServiceImpl.class);

    @Value("${sso.token-url}")
    private String tokenUrl;

    @Value("${sso.userinfo-url}")
    private String userinfoUrl;

    @Value("${sso.redirect-uri}")
    private String redirectUri;

    @Value("${sso.auth-header}")
    private String authHeader;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public GovBRUserProfileResponse getUserProfile(String code, String codeVerifier) throws GovBRException {
        try {
            logger.info("Getting token from GovBR");
            TokenResponseDTO tokenResponse = getToken(code, codeVerifier);
            String accessToken = tokenResponse.getAccess_token();
            logger.info("Token received, fetching user info");
            GovBRUserProfileResponse userProfile = getUserInfo(accessToken);
            logger.info("User info received, fetching profile picture");
            String profilePictureBase64 = getProfilePicture(accessToken, userProfile.getPicture());
            userProfile.setProfilePictureBase64(profilePictureBase64);
            return userProfile;
        } catch (GovBRException e) {
            // Propagate GovBRException as is, so status and message are preserved
            throw e;
        } catch (Exception e) {
            logger.error("Error in getUserProfile: {}", e.getMessage(), e);
            throw new GovBRException("Failed to get user profile", e);
        }
    }

    public TokenResponseDTO getToken(String code, String codeVerifier) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", authHeader);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("code_verifier", codeVerifier);
        map.add("redirect_uri", redirectUri);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        try {
            TokenResponseDTO response = restTemplate.postForObject(tokenUrl, request, TokenResponseDTO.class);
            if (response == null) {
                throw new IdpException("Exception occurred while performing the authorization");
            }
            return response;
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            logger.error("Token request failed: {}", responseBody);
            HttpStatus status = (HttpStatus) e.getStatusCode();
            if (status == HttpStatus.BAD_REQUEST && responseBody != null && responseBody.contains("invalid_grant")) {
                throw new InvalidCodeException("Invalid or expired authorization code", e);
            } else if (status == HttpStatus.UNAUTHORIZED) {
                throw new UnauthorizedClientException("Unauthorized: Invalid client credentials", e);
            }
            throw new TokenRequestException("Token request failed", e, status);
        } catch (Exception e) {
            logger.error("Token request error: {}", e.getMessage());
            throw new TokenRequestException("Token request error", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private GovBRUserProfileResponse getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(userinfoUrl, HttpMethod.GET, request, String.class);
            JsonNode node = objectMapper.readTree(response.getBody());
            GovBRUserProfileResponse userProfile = new GovBRUserProfileResponse();
            userProfile.setSub(node.path("sub").asText());
            userProfile.setName(node.path("name").asText());
            userProfile.setSocialName(node.path("social_name").asText(null));
            userProfile.setProfile(node.path("profile").asText(null));
            userProfile.setPicture(node.path("picture").asText(null));
            userProfile.setEmail(node.path("email").asText(null));
            userProfile.setEmailVerified(node.path("email_verified").asBoolean(false));
            return userProfile;
        } catch (HttpClientErrorException e) {
            logger.error("User info request failed: {}", e.getResponseBodyAsString());
            throw new UserInfoRequestException("User info request failed", e, (HttpStatus) e.getStatusCode());
        } catch (Exception e) {
            logger.error("User info request error: {}", e.getMessage());
            throw new UserInfoRequestException("User info request error", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getProfilePicture(String accessToken, String pictureUrl) {
        if (pictureUrl == null || pictureUrl.isEmpty()) return null;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(pictureUrl, HttpMethod.GET, request, byte[].class);
            return Base64.getEncoder().encodeToString(response.getBody());
        } catch (Exception e) {
            logger.warn("Profile picture fetch failed: {}", e.getMessage());
            return null;
        }
    }
}

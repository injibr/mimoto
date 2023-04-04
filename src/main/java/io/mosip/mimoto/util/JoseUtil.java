package io.mosip.mimoto.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.X509CertUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.mimoto.core.http.ResponseWrapper;
import io.mosip.mimoto.dto.mimoto.JwkDto;
import io.mosip.mimoto.dto.mimoto.WalletBindingInternalResponseDto;
import io.mosip.mimoto.dto.mimoto.WalletBindingResponseDto;
import org.jose4j.jws.JsonWebSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

@Component
public class JoseUtil {

    private final Logger logger = LoggerUtil.getLogger(JoseUtil.class);
    private static final String BEGIN_KEY = "-----BEGIN PUBLIC KEY-----";
    private static final String END_KEY = "-----END PUBLIC KEY-----";

    @Autowired
    private ObjectMapper mapper;

    public static JwkDto getJwkFromPublicKey(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {

        RSAPublicKeySpec keySpec = publicKeyString.startsWith(BEGIN_KEY) ? getKeySpecForPublicKey(publicKeyString) : getKeySpecForRSAKey(publicKeyString);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        // Convert to JWK format
        JWK jwk = new RSAKey.Builder((RSAPublicKey)publicKey)
                .keyUse(KeyUse.SIGNATURE)
                .keyID(UUID.randomUUID().toString())
                .build();

        JwkDto jwkDto = new JwkDto(jwk.getKeyType().toString(), ((RSAKey) jwk).getPublicExponent().toString(), ((RSAKey) jwk).getModulus().toString());
        return jwkDto;

    }

    private static RSAPublicKeySpec getKeySpecForRSAKey(String publicKeyString) {
        String key = publicKeyString.replaceAll("\\n", "")
                .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----END RSA PUBLIC KEY-----", "");

        org.bouncycastle.asn1.pkcs.RSAPublicKey pkcs1PublicKey = org.bouncycastle.asn1.pkcs.RSAPublicKey.getInstance(CryptoUtil.decodePlainBase64(key));
        BigInteger modulus = pkcs1PublicKey.getModulus();
        BigInteger publicExponent = pkcs1PublicKey.getPublicExponent();
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, publicExponent);
        return keySpec;

    }

    private static RSAPublicKeySpec getKeySpecForPublicKey(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String key = publicKeyString.replaceAll("\\n", "")
                .replace(BEGIN_KEY, "")
                .replace(END_KEY, "");
        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(CryptoUtil.decodePlainBase64(key));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey generatePublic = (RSAPublicKey) kf.generatePublic(spec);
        BigInteger modulus = generatePublic.getModulus();
        BigInteger exponent = generatePublic.getPublicExponent();
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
        return keySpec;
    }


    public ResponseWrapper<WalletBindingResponseDto> addThumbprintAndKeyId(ResponseWrapper<WalletBindingInternalResponseDto> responseDto) {

        logger.debug("Inside addThumbprintAndKeyId ");

        ResponseWrapper<WalletBindingResponseDto> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(responseDto.getId());
        responseWrapper.setResponsetime(responseDto.getResponsetime());
        responseWrapper.setVersion(responseDto.getVersion());
        responseWrapper.setErrors(responseDto.getErrors());

        try {
            if (responseDto.getResponse() != null) {
                WalletBindingInternalResponseDto resp = mapper.readValue(
                        mapper.writeValueAsString(responseDto.getResponse()), WalletBindingInternalResponseDto.class);
                // generate thumbprint and key id
                String certificate = resp.getCertificate();
                X509Certificate x509Certificate = X509CertUtils.parse(certificate);
                JsonWebSignature jwSign = new JsonWebSignature();
                jwSign.setKeyIdHeaderValue(x509Certificate.getSerialNumber().toString(10));
                jwSign.setX509CertSha256ThumbprintHeaderValue(x509Certificate);

                // map fields with thumbprint and key id
                WalletBindingResponseDto response = new WalletBindingResponseDto();
                response.setCertificate(resp.getCertificate());
                response.setEncryptedWalletBindingId(resp.getWalletUserId());
                response.setExpireDateTime(resp.getExpireDateTime());
                response.setKeyId(jwSign.getKeyIdHeaderValue());
                response.setThumbprint(jwSign.getX509CertSha256ThumbprintHeaderValue());

                responseWrapper.setResponse(response);
            }
        } catch (Exception e) {
            logger.error("Exception occured while setting thumbprint ", e);
        }

        return responseWrapper;
    }
}

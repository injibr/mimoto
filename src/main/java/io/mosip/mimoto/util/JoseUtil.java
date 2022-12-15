package io.mosip.mimoto.util;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.mimoto.dto.mimoto.JwkDto;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.UUID;

public class JoseUtil {

    public static JwkDto getJwkFromPublicKey(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String key = publicKeyString.replaceAll("\\n", "")
                .replace("-----BEGIN RSA PUBLIC KEY-----", "")
                .replace("-----END RSA PUBLIC KEY-----", "");

        org.bouncycastle.asn1.pkcs.RSAPublicKey pkcs1PublicKey = org.bouncycastle.asn1.pkcs.RSAPublicKey.getInstance(CryptoUtil.decodePlainBase64(key));
        BigInteger modulus = pkcs1PublicKey.getModulus();
        BigInteger publicExponent = pkcs1PublicKey.getPublicExponent();
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, publicExponent);

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
}

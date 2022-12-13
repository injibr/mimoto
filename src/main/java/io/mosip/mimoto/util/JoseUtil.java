package io.mosip.mimoto.util;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import io.mosip.kernel.core.util.CryptoUtil;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.UUID;

public class JoseUtil {


//    public static void main(String[] args) {
//        try {
//            // Generate the RSA key pair
//            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
//            gen.initialize(2048);
//            KeyPair keyPair = gen.generateKeyPair();
//
//            String pk = "-----BEGIN RSA PUBLIC KEY-----\nMIICCgKCAgEArPhI8uHNssl6Wj/T4Px4MGvjQrnCpih+/rsGOmcIml56+0kvQJpb\nfQQ0cDW5BRZrRruCMtbSbVFdZaYgUs9a8UoLujL0djyfdfrDv+a6bqRomK3s9/q4\ndNirsqVTm8WI7oIODa5IuuXQU+YEsnpazpb+cdtbvXwJOvTo+/bSIIX4IWDOPiZM\nt9AGfdw7afRBrQHHyenENW9ACZek/jqu0KEUTqbZb7BsGc2vvyBZrZVfX7PhhtJU\n5JH9kuJltvacHJytA5wUmDydIDONO+YgXPTOfqBQh+u9GxvA0P+kUOxLi1XT/YEW\nBvNc1NJb1FGoG9rO63MP0olq5if2zFryy+BjuGErUj3CTclIk2XhWrAPdwFvRCQI\nuaVlvJk81NNqdPtaNW1UdGpObFgyZCmAXjDHzm/Ou3yvo4z70RTQB27JeeMEADF8\nGrkucKy1s5i8LPRwMwo+kX0fuq9CphyKW7TCavA+idwFQxnzLP3BhtZcDMLUXmQQ\nIKYPnuYoIRPp6CHAjtkWm0zZuhdIhehepSeDO+GIuCkXCvabvMriWcgTRK4xViS3\nT6V4JqNMD5aZDWytjYZuWCAb/E/PdeatqaE5Go7dG27rjdhFIIlKXgUBvu72ZGp9\nPvbx+N7hXgJFN7zV3SLn+CYEICdIeWubznrmsw7O9sFGpnArr+C6P68CAwEAAQ==\n-----END RSA PUBLIC KEY-----\n";
//            String pk2 = "-----BEGIN RSA PUBLIC KEY-----\nMIICCgKCAgEAn6+frMlD7DQqbxZW943hRLBApDj1/lHIJdLYSKEGIfwhd58gc0Y4\n1q11mPnpv7gAZ/Wm0iOAkWSzcIWljXFmGnLrUrBsp4WYKdPjqn4tkrCOjiZa5RPk\nY03a40Kz1lx0W9f94Naozglf6KFUSq+qAwuC5kiPxaxsjFA/LWIP+zT2QX/MnrX9\nv7gt2g0BC4pQ01eTTzhhwO2A7k5z3ucsb56ohND4xdIsdCMm1IczBjW0URSO60Bb\n7m5dlO8BFHJ6inV8awO2KHoADbp3wZgid4KqLJ0eVGyNViVFzj4rxSxL3vcYbyKS\nORWSlPZIZL9ZWO1cyPO9+Wxu29IKj4DQEt8glgITlBZ4L29uT7gFPAbypSn/8SvU\nBrNno8+GIe9XWsrDTMT9dfLGzLUitF3A+wwVZuRVhCqYIisOOGuGE18YK0jmdk9l\n89OpK4PduGiUh66zZTcH3thdtaOz6jj+FLKMg2Q3gNqQ1Y0cezO175RNVVX1ffOu\n5qss1RWams5RAXDqqt/MhiopG3DhlyaSC4xdqei7SI8d+S4Bvflub9rypPnhW67g\nNhZvQDJ7Tb1AWHxKmU0wQvEMtwSm9xtsMs4bqotn2M/09BuRqbrhpvAfrfZArkVO\nv8eLXhtDvo2J9gRwHZIS/JZ1Fo+tep1QFHz1Lr5iGRqwLWQlGbKFuL0CAwEAAQ==\n-----END RSA PUBLIC KEY-----\n";
//            JWK jwk = getJwkFromPublicKey(pk2);
//            System.out.println(jwk);
////            org.bouncycastle.asn1.pkcs.RSAPublicKey pkcs1PublicKey = org.bouncycastle.asn1.pkcs.RSAPublicKey.getInstance(io.mosip.kernel.core.util.CryptoUtil.decodePlainBase64(key));
////            BigInteger modulus = pkcs1PublicKey.getModulus();
////            BigInteger publicExponent = pkcs1PublicKey.getPublicExponent();
////            RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, publicExponent);
////            //KeyFactory kf = KeyFactory.getInstance("RSA");
////            //PublicKey generatedPublic = kf.generatePublic(keySpec);
////
////           // String publicKeyPemStr = key.replace("-----BEGIN RSA PUBLIC KEY-----\n", "");
////            String publicKeyPemStr = key.replaceAll("\\n", "").replace("-----BEGIN RSA PUBLIC KEY-----", "").replace("-----END RSA PUBLIC KEY-----", "");;
////            byte[] encoded =  io.mosip.kernel.core.util.CryptoUtil.decodePlainBase64(publicKeyPemStr);
////
////            //X509EncodedKeySpec  keySpec = new X509EncodedKeySpec(encoded);
////            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
////            PublicKey publicKey = keyFactory.generatePublic(keySpec);
////            // Convert to JWK format
////            JWK jwk = new RSAKey.Builder((RSAPublicKey)publicKey)
////                    .keyUse(KeyUse.SIGNATURE)
////                    .keyID(UUID.randomUUID().toString())
////                    .build();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }


    public static JWK getJwkFromPublicKey(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
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

        return jwk;

    }
}

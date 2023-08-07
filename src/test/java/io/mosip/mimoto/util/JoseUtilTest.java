package io.mosip.mimoto.util;

import io.mosip.mimoto.dto.mimoto.JwkDto;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.assertEquals;

public class JoseUtilTest {
    @Test
    public void getJwkFromPublicKeyTest() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String publicKey = "-----BEGIN RSA PUBLIC KEY-----\nMIICCgKCAgEAn6+frMlD7DQqbxZW943hRLBApDj1/lHIJdLYSKEGIfwhd58gc0Y4\n1q11mPnpv7gAZ/Wm0iOAkWSzcIWljXFmGnLrUrBsp4WYKdPjqn4tkrCOjiZa5RPk\nY03a40Kz1lx0W9f94Naozglf6KFUSq+qAwuC5kiPxaxsjFA/LWIP+zT2QX/MnrX9\nv7gt2g0BC4pQ01eTTzhhwO2A7k5z3ucsb56ohND4xdIsdCMm1IczBjW0URSO60Bb\n7m5dlO8BFHJ6inV8awO2KHoADbp3wZgid4KqLJ0eVGyNViVFzj4rxSxL3vcYbyKS\nORWSlPZIZL9ZWO1cyPO9+Wxu29IKj4DQEt8glgITlBZ4L29uT7gFPAbypSn/8SvU\nBrNno8+GIe9XWsrDTMT9dfLGzLUitF3A+wwVZuRVhCqYIisOOGuGE18YK0jmdk9l\n89OpK4PduGiUh66zZTcH3thdtaOz6jj+FLKMg2Q3gNqQ1Y0cezO175RNVVX1ffOu\n5qss1RWams5RAXDqqt/MhiopG3DhlyaSC4xdqei7SI8d+S4Bvflub9rypPnhW67g\nNhZvQDJ7Tb1AWHxKmU0wQvEMtwSm9xtsMs4bqotn2M/09BuRqbrhpvAfrfZArkVO\nv8eLXhtDvo2J9gRwHZIS/JZ1Fo+tep1QFHz1Lr5iGRqwLWQlGbKFuL0CAwEAAQ==\n-----END RSA PUBLIC KEY-----\n";
        JwkDto expectedJWK = new JwkDto("RSA", "AQAB", "n6-frMlD7DQqbxZW943hRLBApDj1_lHIJdLYSKEGIfwhd58gc0Y41q11mPnpv7gAZ_Wm0iOAkWSzcIWljXFmGnLrUrBsp4WYKdPjqn4tkrCOjiZa5RPkY03a40Kz1lx0W9f94Naozglf6KFUSq-qAwuC5kiPxaxsjFA_LWIP-zT2QX_MnrX9v7gt2g0BC4pQ01eTTzhhwO2A7k5z3ucsb56ohND4xdIsdCMm1IczBjW0URSO60Bb7m5dlO8BFHJ6inV8awO2KHoADbp3wZgid4KqLJ0eVGyNViVFzj4rxSxL3vcYbyKSORWSlPZIZL9ZWO1cyPO9-Wxu29IKj4DQEt8glgITlBZ4L29uT7gFPAbypSn_8SvUBrNno8-GIe9XWsrDTMT9dfLGzLUitF3A-wwVZuRVhCqYIisOOGuGE18YK0jmdk9l89OpK4PduGiUh66zZTcH3thdtaOz6jj-FLKMg2Q3gNqQ1Y0cezO175RNVVX1ffOu5qss1RWams5RAXDqqt_MhiopG3DhlyaSC4xdqei7SI8d-S4Bvflub9rypPnhW67gNhZvQDJ7Tb1AWHxKmU0wQvEMtwSm9xtsMs4bqotn2M_09BuRqbrhpvAfrfZArkVOv8eLXhtDvo2J9gRwHZIS_JZ1Fo-tep1QFHz1Lr5iGRqwLWQlGbKFuL0");

        JwkDto actualJWK = JoseUtil.getJwkFromPublicKey(publicKey);

        assertEquals(expectedJWK, actualJWK);
    }

}
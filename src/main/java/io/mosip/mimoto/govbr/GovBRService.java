package io.mosip.mimoto.govbr;

import io.mosip.mimoto.govbr.exceptions.GovBRException;

public interface GovBRService {
    GovBRUserProfileResponse getUserProfile(String code, String codeVerifier) throws GovBRException;
}


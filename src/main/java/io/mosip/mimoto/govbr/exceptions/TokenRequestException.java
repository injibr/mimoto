package io.mosip.mimoto.govbr.exceptions;

import org.springframework.http.HttpStatus;

public class TokenRequestException extends GovBRException {
    public TokenRequestException(String message, Throwable cause, HttpStatus status) {
        super(message, cause, status);
    }
}


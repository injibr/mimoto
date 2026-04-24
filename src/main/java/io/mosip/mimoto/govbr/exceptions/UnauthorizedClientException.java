package io.mosip.mimoto.govbr.exceptions;

import org.springframework.http.HttpStatus;

public class UnauthorizedClientException extends GovBRException {
    public UnauthorizedClientException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED);
    }
}


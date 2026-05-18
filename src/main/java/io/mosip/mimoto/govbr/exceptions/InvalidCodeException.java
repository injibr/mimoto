package io.mosip.mimoto.govbr.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidCodeException extends GovBRException {
    public InvalidCodeException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }
}


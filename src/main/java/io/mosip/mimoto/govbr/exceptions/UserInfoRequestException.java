package io.mosip.mimoto.govbr.exceptions;

import org.springframework.http.HttpStatus;

public class UserInfoRequestException extends GovBRException {
    public UserInfoRequestException(String message, Throwable cause, HttpStatus status) {
        super(message, cause, status);
    }
}


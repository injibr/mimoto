package io.mosip.mimoto.util;


import io.mosip.mimoto.dto.ErrorDTO;
import io.mosip.mimoto.dto.resident.CredentialRequestResponseDTO;
import io.mosip.mimoto.exception.InvalidInputException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;

@ControllerAdvice
public class CommonExceptionHandler{
    @ExceptionHandler( value = InvalidInputException.class)
    public ResponseEntity<CredentialRequestResponseDTO> handleInvalidInput(InvalidInputException ex) {
        CredentialRequestResponseDTO credentialRequestResponseDTO = new CredentialRequestResponseDTO();
        ErrorDTO errors = new ErrorDTO(ex.getErrorCode(), ex.getMessage());
        credentialRequestResponseDTO.setVersion("1.0");
        credentialRequestResponseDTO.setErrors(Collections.singletonList(errors));
        return new ResponseEntity<>(credentialRequestResponseDTO, HttpStatus.BAD_REQUEST);
    }

}

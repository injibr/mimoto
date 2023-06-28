package io.mosip.mimoto.util;

import io.mosip.mimoto.exception.InvalidInputException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;


@Component
public class ValidationUtil {
    public static void validateInputRequest(BindingResult result){
        if(result.hasErrors()){
            FieldError fieldError = result.getFieldError();
            if(fieldError != null) {
                String errorMessage = fieldError.getField() + " " + fieldError.getDefaultMessage();
                throw new InvalidInputException(errorMessage);
            }
        }
    }
}

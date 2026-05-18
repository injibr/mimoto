package io.mosip.mimoto.govbr;

import io.mosip.mimoto.govbr.exceptions.GovBRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@ControllerAdvice
public class GovBRExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GovBRExceptionHandler.class);

    @ExceptionHandler(GovBRException.class)
    public ResponseEntity<ApiResponse<Object>> handleGovBRException(GovBRException ex) {
        logger.error("GovBRException: {}", ex.getMessage(), ex);
        int code = ex.getHttpStatus().value();
        ApiResponse<Object> response = new ApiResponse<>(
                "error",
                code,
                ex.getMessage(),
                null,
                DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        ApiResponse<Object> response = new ApiResponse<>(
                "error",
                500,
                "Internal server error",
                null,
                DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

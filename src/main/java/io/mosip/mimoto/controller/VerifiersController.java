package io.mosip.mimoto.controller;

import io.mosip.mimoto.core.http.ResponseWrapper;
import io.mosip.mimoto.dto.ErrorDTO;
import io.mosip.mimoto.dto.openid.VerifiersDTO;
import io.mosip.mimoto.exception.ApiNotAccessibleException;
import io.mosip.mimoto.service.VerifierService;
import io.mosip.mimoto.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static io.mosip.mimoto.exception.PlatformErrorMessages.API_NOT_ACCESSIBLE_EXCEPTION;

@RestController
@RequestMapping(value = "/verifiers")
public class VerifiersController {

    @Autowired
    VerifierService verifierService;

    private static final String ID = "mosip.mimoto.verifier";

    private final Logger logger = LoggerFactory.getLogger(VerifiersController.class);

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getAllTrustedVerifiers() {
        ResponseWrapper<VerifiersDTO> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setId(ID);
        responseWrapper.setVersion("v1");
        responseWrapper.setResponsetime(DateUtils.getRequestTimeString());
        try {
            responseWrapper.setResponse(verifierService.getTrustedVerifiers());
        } catch (ApiNotAccessibleException | IOException e) {
            logger.error("Exception occurred while fetching trusted verifiers ", e);
            responseWrapper.setErrors(List.of(new ErrorDTO(API_NOT_ACCESSIBLE_EXCEPTION.getCode(), API_NOT_ACCESSIBLE_EXCEPTION.getMessage())));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseWrapper);
        }

        return ResponseEntity.status(HttpStatus.OK).body(responseWrapper);
    }
}

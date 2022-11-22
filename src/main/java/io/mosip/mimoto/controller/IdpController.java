package io.mosip.mimoto.controller;

import com.google.gson.Gson;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.mimoto.core.http.RequestWrapper;
import io.mosip.mimoto.dto.mimoto.AppOTPRequestDTO;
import io.mosip.mimoto.dto.mimoto.IdpCredentialRequestDTO;
import io.mosip.mimoto.dto.resident.*;
import io.mosip.mimoto.service.impl.CredentialShareServiceImpl;
import io.mosip.mimoto.util.DateUtils;
import io.mosip.mimoto.util.LoggerUtil;
import io.mosip.mimoto.util.Utilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
public class IdpController {

    private final Logger logger = LoggerUtil.getLogger(IdpController.class);

    private Gson gson = new Gson();

    @Value("${mosip.idp.partner.encryption.key}")
    private String partnerEncryptionKey;

    @Value("${mosip.idp.partner.id}")
    private String partnerId;


    // TODO: Temporary mocking, need to cleanup
    @Value("${mimoto.mocked.requestid}")
    String mockedReqid;

    @Autowired
    private Utilities utilities;

    @PostMapping("/binding-otp")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> otpRequest(@RequestBody AppOTPRequestDTO requestDTO) throws Exception {
        OTPRequestDTO mosipOTPRequestPayload = new OTPRequestDTO();
        mosipOTPRequestPayload.setVersion("1.0");
        mosipOTPRequestPayload.setId("mosip.identity.otp.internal");
        mosipOTPRequestPayload.setIndividualId(requestDTO.getIndividualId());
        mosipOTPRequestPayload.setIndividualIdType(requestDTO.getIndividualIdType());
        mosipOTPRequestPayload.setOtpChannel(requestDTO.getOtpChannel());
        mosipOTPRequestPayload.setRequestTime(DateUtils.getRequestTimeString());
        mosipOTPRequestPayload.setTransactionID(requestDTO.getTransactionID());

        // mocked response
        OTPResponseDTO response = new OTPResponseDTO();
        /*(ResponseWrapper<CredentialRequestResponseDTO>) restClientService
                .postApi(ApiName.RESIDENT_OTP, "", "", mosipOTPRequestPayload, ResponseWrapper.class, MediaType.APPLICATION_JSON);*/
        OTPResponseMaskedDTO otpResponseMaskedDTO = new OTPResponseMaskedDTO();
        otpResponseMaskedDTO.setMaskedEmail("XXcXXfXXpXXjXXhXXzXXxXXsXXsXXcXXjXXyXXqXXuXXdXXiXXtXXqXXzXX2XX@mailinator.com");
        otpResponseMaskedDTO.setMaskedMobile("XXXXXX91723");
        response.setId("mosip.identity.otp.internal");
        response.setVersion("1.0");
        response.setResponse(otpResponseMaskedDTO);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = "/binding-credential-request", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> request(@RequestBody IdpCredentialRequestDTO requestDTO)
            throws Exception {


        logger.info("Received binding-credential-request : " + JsonUtils.javaObjectToJsonString(requestDTO));

        // Call mosip resident service to issue a new credential
        RequestWrapper<CredentialRequestDTO> mosipCredentialRequestPayload = new RequestWrapper<CredentialRequestDTO>();

        CredentialRequestDTO credentialReqDTO = new CredentialRequestDTO();
        credentialReqDTO.setEncryptionKey(partnerEncryptionKey);
        credentialReqDTO.setIssuer(partnerId);
        credentialReqDTO.setIndividualId(requestDTO.getIndividualId());
        credentialReqDTO.setOtp(requestDTO.getOtp());
        credentialReqDTO.setTransactionID(requestDTO.getTransactionID());
        credentialReqDTO.setCredentialType(requestDTO.getCredentialType());
        credentialReqDTO.setUser(requestDTO.getUser());

        mosipCredentialRequestPayload.setId("mosip.resident.vid");
        mosipCredentialRequestPayload.setVersion("v1");
        mosipCredentialRequestPayload.setRequesttime(DateUtils.getRequestTimeString());
        mosipCredentialRequestPayload.setRequest(credentialReqDTO);

        CredentialRequestResponseDTO response = new CredentialRequestResponseDTO();
        /*(CredentialRequestResponseDTO)restClientService
                .postApi(ApiName.RESIDENT_CREDENTIAL_REQUEST, "", "", mosipCredentialRequestPayload,
                        CredentialRequestResponseDTO.class, MediaType.APPLICATION_JSON);*/
        CredentialRequestResponseInnerResponseDTO innerResponseDTO = new CredentialRequestResponseInnerResponseDTO();
        innerResponseDTO.setId("2309356374");
        innerResponseDTO.setRequestId(mockedReqid);

        response.setId("mosip.credential.request.service.id");
        response.setResponsetime(io.mosip.kernel.core.util.DateUtils.getCurrentDateTimeString());
        response.setResponse(innerResponseDTO);

        // Create request id file for later event.
        if (CollectionUtils.isEmpty(response.getErrors())) {
            Path vcRequestIdPath = Path.of(
                    utilities.getDataPath(),
                    String.format(CredentialShareServiceImpl.VC_REQUEST_FILE_NAME, response.getResponse().getRequestId())
            );
            Files.write(vcRequestIdPath, gson.toJson(response).getBytes());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

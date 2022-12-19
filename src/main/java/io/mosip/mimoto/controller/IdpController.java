package io.mosip.mimoto.controller;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.mimoto.constant.ApiName;
import io.mosip.mimoto.core.http.ResponseWrapper;
import io.mosip.mimoto.dto.ErrorDTO;
import io.mosip.mimoto.dto.mimoto.*;
import io.mosip.mimoto.exception.ApisResourceAccessException;
import io.mosip.mimoto.exception.PlatformErrorMessages;
import io.mosip.mimoto.service.RestClientService;
import io.mosip.mimoto.util.DateUtils;
import io.mosip.mimoto.util.JoseUtil;
import io.mosip.mimoto.util.LoggerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class IdpController {

    private final Logger logger = LoggerUtil.getLogger(IdpController.class);
    private static final boolean useBearerToken = true;
    private static final String ID = "mosip.mimoto.idp";
    private Gson gson = new Gson();

    @Autowired
    public RestClientService<Object> restClientService;

    @PostMapping("/binding-otp")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> otpRequest(@RequestBody BindingOtpRequestDto requestDTO) throws Exception {
        logger.debug("Received binding-otp request : " + JsonUtils.javaObjectToJsonString(requestDTO));
        ResponseWrapper<BindingOtpResponseDto> response = null;
        try {
            response = (ResponseWrapper<BindingOtpResponseDto>) restClientService
                    .postApi(ApiName.BINDING_OTP,
                            requestDTO, ResponseWrapper.class, useBearerToken);
        } catch (Exception e) {
            logger.error("Wallet binding otp error occured.", e);
            response = getErrorResponse(PlatformErrorMessages.MIMOTO_OTP_BINDING_EXCEPTION.getCode(), e.getMessage());
        }


        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(path = "/wallet-binding", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> request(@RequestBody WalletBindingRequestDTO requestDTO)
            throws Exception {

        logger.debug("Received wallet-binding request : " + JsonUtils.javaObjectToJsonString(requestDTO));

        ResponseWrapper<WalletBindingResponseDto> response = null;
        try {
            WalletBindingInnerRequestDto innerRequestDto = new WalletBindingInnerRequestDto();
            innerRequestDto.setChallengeList(requestDTO.getRequest().getChallengeList());
            innerRequestDto.setIndividualId(requestDTO.getRequest().getIndividualId());
            innerRequestDto.setTransactionId(requestDTO.getRequest().getTransactionId());
            innerRequestDto.setPublicKey(JoseUtil.getJwkFromPublicKey(requestDTO.getRequest().getPublicKey()));

            WalletBindingInternalRequestDTO req = new WalletBindingInternalRequestDTO(requestDTO.getRequestTime(), innerRequestDto);

            response = (ResponseWrapper<WalletBindingResponseDto>) restClientService
                    .postApi(ApiName.WALLET_BINDING,
                            req, ResponseWrapper.class, useBearerToken);
        } catch (Exception e) {
            logger.error("Wallet binding error occured for tranaction id " + requestDTO.getRequest().getTransactionId(), e);
            response = getErrorResponse(PlatformErrorMessages.MIMOTO_WALLET_BINDING_EXCEPTION.getCode(), e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/link-transaction")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> linkTransaction(@RequestBody LinkTransactionRequestDto requestDTO) throws Exception {
        LinkTransactionResponseDto res = null;
        try {
            IdpLinkCodeDto dto = new IdpLinkCodeDto(requestDTO.getLinkCode());
            IdpLinkTransactionReqDto reqDto = new IdpLinkTransactionReqDto(requestDTO.getRequestTime(), dto);
            res = (LinkTransactionResponseDto) restClientService
                    .postApi(ApiName.IDP_LINK_TRANSACTION, null, null,
                            reqDto, LinkTransactionResponseDto.class);
        } catch (Exception e) {
            logger.error("link-transaction error error occured for link code " + requestDTO.getLinkCode(), e);
            res = new LinkTransactionResponseDto(DateUtils.getRequestTimeString(), null,
                    getErrors(PlatformErrorMessages.MIMOTO_LINK_TRANSACTION_EXCEPTION.getCode(), e.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("idp-authenticate")
    public ResponseEntity<Object> idpAuthenticate(@RequestBody IdpAuthRequestDto requestDTO) throws Exception {
        logger.debug("Received idp-authenticate request : " + JsonUtils.javaObjectToJsonString(requestDTO));

        ResponseWrapper<LinkedTransactionResponseDto> response = null;

        try {
            // calling mock otp code which should be removed once idp backend is ready
            callIdpOtpApi(requestDTO);

            response = (ResponseWrapper<LinkedTransactionResponseDto>) restClientService
                    .postApi(ApiName.IDP_AUTHENTICATE, null, null,
                            requestDTO, ResponseWrapper.class);
        } catch (Exception e) {
            logger.error("idp-authenticate error error occured for link code " + requestDTO.getRequest().getLinkedTransactionId(), e);
            response = getErrorResponse(PlatformErrorMessages.MIMOTO_WALLET_BINDING_EXCEPTION.getCode(), e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("idp-consent")
    public ResponseEntity<Object> idpLinkCode(@RequestBody IdpConsentDto requestDTO) throws Exception {
        logger.debug("Received idp-link-code request : " + JsonUtils.javaObjectToJsonString(requestDTO));

        ResponseWrapper<LinkedTransactionResponseDto> response = null;
        try {
            response = (ResponseWrapper<LinkedTransactionResponseDto>) restClientService
                    .postApi(ApiName.IDP_CONSENT, null, null,
                            requestDTO, ResponseWrapper.class);
        } catch (Exception e) {
            logger.error("link-transaction error error occured for link code " + requestDTO.getRequest().getLinkedTransactionId(), e);
            response = getErrorResponse(PlatformErrorMessages.MIMOTO_WALLET_BINDING_EXCEPTION.getCode(), e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    private ResponseWrapper getErrorResponse(String errorCode, String errorMessage) {

        List<ErrorDTO> errors = getErrors(errorCode, errorMessage);
        ResponseWrapper responseWrapper = new ResponseWrapper();
        responseWrapper.setResponse(null);
        responseWrapper.setResponsetime(DateUtils.getRequestTimeString());
        responseWrapper.setId(ID);
        responseWrapper.setErrors(errors);

        return responseWrapper;
    }

    private List<ErrorDTO> getErrors(String errorCode, String errorMessage) {
        ErrorDTO errorDTO = new ErrorDTO(errorCode, errorMessage);
        return Lists.newArrayList(errorDTO);
    }

    /**
     * To be cleaned up later
     *
     * @param requestDTO
     * @return
     */
    private void callIdpOtpApi(IdpAuthRequestDto requestDTO) throws ApisResourceAccessException {

        IdpOtpReq req = new IdpOtpReq();
        req.setRequestTime(requestDTO.getRequestTime());

        IdpOtpReqDto internalRequest = new IdpOtpReqDto(requestDTO.getRequest().getLinkedTransactionId(),
                requestDTO.getRequest().getIndividualId(), Lists.newArrayList("EMAIL"));

        req.setRequest(internalRequest);

        ResponseWrapper<BindingOtpResponseDto> response = (ResponseWrapper<BindingOtpResponseDto>) restClientService
                .postApi(ApiName.IDP_OTP, null, null,
                        req, ResponseWrapper.class);

        if (response.getResponse() == null)
            throw new ApisResourceAccessException("Idp otp api not accessible");

    }
}

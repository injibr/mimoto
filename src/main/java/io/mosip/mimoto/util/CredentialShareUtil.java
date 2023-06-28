package io.mosip.mimoto.util;

import com.fasterxml.jackson.databind.JsonNode;
import io.mosip.mimoto.dto.mimoto.CredentialDownloadRequestDTO;
import io.mosip.mimoto.exception.InvalidInputException;
import io.mosip.mimoto.exception.PlatformErrorMessages;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CredentialShareUtil {
    public void validateCredentialDownloadRequest(CredentialDownloadRequestDTO requestDTO, JsonNode requestedCredentialJSON) throws IOException, NoSuchFieldException {
        if(requestedCredentialJSON.has("response") && requestedCredentialJSON.get("response").has("id")) {
            String requestedIndividualId = requestedCredentialJSON.get("response").get("id").asText();
            if (!requestDTO.getIndividualId().equals(requestedIndividualId)) {
                throw new InvalidInputException(PlatformErrorMessages.MIMOTO_PGS_INVALID_INPUT_PARAMETER.getMessage() + " - " + "individualId");
            }
        }
    }
}

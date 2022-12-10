package io.mosip.mimoto.dto.mimoto;

import io.mosip.mimoto.dto.ErrorDTO;
import lombok.Data;

import java.util.List;

@Data
public class LinkTransactionResponseDto {

    private String responseTime;

    private LinkCodeResponse response;

    private List<ErrorDTO> errors;
}

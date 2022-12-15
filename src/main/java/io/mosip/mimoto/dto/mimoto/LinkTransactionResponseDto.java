package io.mosip.mimoto.dto.mimoto;

import io.mosip.mimoto.dto.ErrorDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkTransactionResponseDto {

    private String responseTime;

    private LinkCodeResponse response;

    private List<ErrorDTO> errors;
}

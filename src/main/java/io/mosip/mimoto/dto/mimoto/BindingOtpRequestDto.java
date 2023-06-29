package io.mosip.mimoto.dto.mimoto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BindingOtpRequestDto {
    private String requestTime;

    @Valid
    @NotNull
    private BindingOtpInnerReqDto request;
}

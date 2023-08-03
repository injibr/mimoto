package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class BindingOtpInnerReqDto {
    @NotNull
    private String individualId;
    @NotNull
    @NotEmpty
    private List<String> otpChannels;
}

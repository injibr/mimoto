package io.mosip.mimoto.dto.mimoto;

import lombok.Data;

import java.util.List;

@Data
public class BindingOtpInnerReqDto {
    private String individualId;
    private List<String> otpChannels;
}

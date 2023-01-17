package io.mosip.mimoto.dto.mimoto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternalAidStatusDTO {

	private String individualId;
	private String otp;
	private String transactionId;
	
}

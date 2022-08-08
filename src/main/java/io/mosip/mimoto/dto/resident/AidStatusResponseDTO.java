package io.mosip.mimoto.dto.resident;

import lombok.Data;

@Data
public class AidStatusResponseDTO {

	private String individualId;
	private String individualIdType;
	private String transactionID;
	private String aidStatus;
	
}

package io.mosip.mimoto.dto.openid.presentation;

import java.util.List;

public interface IFormat {
    List<String> getProofTypes();
    void setProofTypes(List<String> proofTypes);
}

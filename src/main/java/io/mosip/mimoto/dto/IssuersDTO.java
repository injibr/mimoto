package io.mosip.mimoto.dto;

import com.google.gson.annotations.Expose;
import lombok.Data;

import java.util.List;

@Data
public class IssuersDTO {
    @Expose
    List<IssuerDTO> issuers;

}
package io.mosip.mimoto.dto;


import com.google.gson.annotations.Expose;
import lombok.Data;

@Data
public class LogoDTO {
    @Expose
    String url;
    @Expose
    String alt_text;
}

package io.mosip.mimoto.dto;

import com.google.gson.annotations.Expose;
import lombok.Data;

@Data

public class DisplayDTO {
    @Expose
    String name;
    @Expose
    LogoDTO logo;
    @Expose
    String title;
    @Expose
    String description;
    @Expose
    String language;
}

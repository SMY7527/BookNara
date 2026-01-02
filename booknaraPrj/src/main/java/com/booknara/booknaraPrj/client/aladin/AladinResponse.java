package com.booknara.booknaraPrj.client.aladin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AladinResponse {
    private String errorCode;
    private String errorMessage;
    private String version;
    private List<AladinDTO> item;
}

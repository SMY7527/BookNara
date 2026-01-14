package com.booknara.booknaraPrj.bookAPI.client.infoNaru;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data

//외부 API변경 대응
@JsonIgnoreProperties(ignoreUnknown = true)
public class InfoNaruDTO {
    @JsonProperty("bookname")
    private String booktitle;
    private String publisher;
    private String isbn13;
}

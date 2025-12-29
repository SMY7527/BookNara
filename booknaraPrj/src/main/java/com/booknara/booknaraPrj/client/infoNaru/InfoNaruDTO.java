package com.booknara.booknaraPrj.client.infoNaru;

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
    private String bookname;
    private String authors;
    private String publisher;
    private String isbn13;

    //JSON필드명과 자바 필드명을 연결
    @JsonProperty("bookImageURL")
    private String bookimageURL;
}

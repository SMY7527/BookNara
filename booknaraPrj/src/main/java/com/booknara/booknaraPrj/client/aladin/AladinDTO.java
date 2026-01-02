package com.booknara.booknaraPrj.client.aladin;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AladinDTO {

    @JsonProperty("pubDate")
    private String pubdate;
    private String cover;
    private String categoryId;
    private String description;

}

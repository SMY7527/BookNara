package com.booknara.booknaraPrj.client.aladin;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "api.aladin")
public class AladinProperties {
    private List<String> keys = new ArrayList<>();
}

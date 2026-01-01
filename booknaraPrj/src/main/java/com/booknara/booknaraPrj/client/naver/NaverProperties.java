package com.booknara.booknaraPrj.client.naver;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "api.naver")
public class NaverProperties {

    private List<Client> clients = new ArrayList<>();

    @Getter
    @Setter
    public static class Client {
        private String id;
        private String secret;
    }
}

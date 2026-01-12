package com.booknara.booknaraPrj.ebook.infrastructure.translate;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class GoogleAccessTokenUtil {
    private final Resource credentialsResource;

    public GoogleAccessTokenUtil(
            @Value("${google.translate.credentials}") Resource credentialsResource
    ) {
        this.credentialsResource = credentialsResource;
    }

    public String getAccessToken() {
        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(credentialsResource.getInputStream())
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new IllegalStateException("Google AccessToken 발급 실패", e);
        }
    }
}

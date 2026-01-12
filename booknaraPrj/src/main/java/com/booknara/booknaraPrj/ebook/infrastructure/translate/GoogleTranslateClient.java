package com.booknara.booknaraPrj.ebook.infrastructure.translate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class GoogleTranslateClient {
    private final String projectId;

    private final RestClient restClient;
    private final GoogleAccessTokenUtil tokenUtil;

    public GoogleTranslateClient(
            GoogleAccessTokenUtil tokenUtil,
            @Value("${google.translate.project-id}") String projectId
    ) {
        this.tokenUtil = tokenUtil;
        this.projectId = projectId;
        this.restClient = RestClient.create();
    }

    @SuppressWarnings("unchecked")
    public List<String> translate(List<String> texts) {
        String accessToken = tokenUtil.getAccessToken();

        Map<String, Object> body = Map.of(
                "contents", texts,
                "sourceLanguageCode", "en",
                "targetLanguageCode", "ko"
        );

        Map<String, Object> response = restClient.post()
                .uri("https://translation.googleapis.com/v3/projects/{project}/locations/global:translateText",
                        projectId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        List<Map<String, String>> translations =
                (List<Map<String, String>>) response.get("translations");

        return translations.stream()
                .map(t -> t.get("translatedText"))
                .toList();
    }
}

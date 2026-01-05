package com.booknara.booknaraPrj.ebook.service;

import com.booknara.booknaraPrj.ebook.dto.DictRespDTO;
import com.booknara.booknaraPrj.ebook.dto.Meaning;
import com.booknara.booknaraPrj.ebook.infrastructure.translate.GoogleTranslateClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor    // lombok 생성자 주입
@Service
public class GoogleDictionaryService {
    private final GoogleTranslateClient client;

    public DictRespDTO getWordMean(String word) {
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.dictionaryapi.dev")
                .build();

        try {
            ResponseEntity<List<DictRespDTO>> response = restClient.get()
                    .uri("https://api.dictionaryapi.dev/api/v2/entries/en/{word}", word)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<>(){});

            DictRespDTO result = response.getBody().get(0);
            System.out.println(result);

            // 번역
            List<Meaning> meanings = result.getMeanings();
            List<Meaning> transMeaning = new ArrayList<>();

            for (Meaning mean : meanings) {
                List<String> translateStr = new ArrayList<>();
                translateStr.add(mean.getPartOfSpeech());
                mean.getDefinitions().forEach(def -> {
                    translateStr.add(def.getDefinition());
                });

                System.out.println(translateStr);

                List<String> translateResult = client.translate(translateStr);
                System.out.println(translateResult);
                mean.setPartOfSpeech(translateResult.get(0));
                System.out.println("1");
                //translateResult.remove(0);
                System.out.println("2");
                for (int i = 1; i < translateResult.size(); i++) {
                    mean.getDefinitions().get(i-1).setDefinition(translateResult.get(i));
                }

                transMeaning.add(mean);
            }

            result.setMeanings_kor(transMeaning);
            System.out.println(result);

            return result;

        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

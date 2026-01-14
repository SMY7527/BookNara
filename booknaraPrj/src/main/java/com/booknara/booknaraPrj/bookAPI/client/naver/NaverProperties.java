package com.booknara.booknaraPrj.bookAPI.client.naver;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * [NaverProperties]
 * 설정 파일(application.yml)의 'api.naver' 경로 값을 바인딩하는 프로퍼티 클래스
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "api.naver")
public class NaverProperties {

    /** * 네이버 애플리케이션 인증 정보(ID, Secret) 목록
     * 다중 클라이언트를 등록하여 호출 한도 분산 및 로테이션 가능
     */
    private List<Client> clients = new ArrayList<>();

    /** 네이버 API 인증 키 쌍 (Client ID & Client Secret) */
    @Getter
    @Setter
    public static class Client {
        private String id;
        private String secret;
    }
}
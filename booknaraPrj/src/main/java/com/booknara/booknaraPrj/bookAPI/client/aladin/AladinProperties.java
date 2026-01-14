package com.booknara.booknaraPrj.bookAPI.client.aladin;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * [AladinProperties]
 * 설정 파일(application.yml)의 'api.aladin' 경로 값을 바인딩하는 프로퍼티 클래스
 */
@Data
@ConfigurationProperties(prefix = "api.aladin")
public class AladinProperties {

    /**
     * 알라딘 API 호출을 위한 TTBKey 목록
     * 다중 키를 지원하여 호출 제한(Traffic Limit) 분산 및 유연한 키 교체 가능
     */
    private List<String> keys = new ArrayList<>();
}
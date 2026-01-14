package com.booknara.booknaraPrj.bookcirculation.status.service;

import com.booknara.booknaraPrj.bookcirculation.status.dto.BookCirculationStatusDTO;
import com.booknara.booknaraPrj.bookcirculation.status.mapper.BookCirculationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * [BookCirculationService]
 * 특정 도서의 전반적인 유동 현황(재고, 대출, 예약)을 조회하여
 * 사용자에게 실시간 가용성 정보를 제공하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class BookCirculationService {

    private final BookCirculationMapper mapper;

    /**
     * [도서 순환 상태 종합 조회]
     * - 특정 도서(ISBN)의 소장 수량 및 현재 이용 현황을 조회합니다.
     * - 로그인한 사용자의 경우, 해당 도서와 관련된 본인의 상태(대출중 여부, 예약 여부 등)를 함께 포함합니다.
     * * @param isbn13 조회할 도서의 고유 번호
     * @param userId 현재 접속 중인 사용자 ID (비로그인 시 null)
     * @return 화면 제어용 플래그가 포함된 통합 상태 DTO
     */
    public BookCirculationStatusDTO getStatus(String isbn13, String userId) {
        // 복잡한 JOIN과 COUNT 연산이 포함된 매퍼를 호출하여 단일 DTO로 반환합니다.
        return mapper.getStatus(isbn13, userId);
    }
}
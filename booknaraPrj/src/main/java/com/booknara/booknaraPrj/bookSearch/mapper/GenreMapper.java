package com.booknara.booknaraPrj.bookSearch.mapper;

import com.booknara.booknaraPrj.bookSearch.dto.GenreDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * [GenreMapper]
 * 도서 카테고리(장르) 조회를 위한 MyBatis 매퍼 인터페이스입니다.
 * 실제 쿼리문은 GenreMapper.xml 파일에 정의되어 있습니다.
 */
@Mapper
public interface GenreMapper {

    /**
     * [국내도서 상위 장르 자동 선별]
     * 도서 보유량이 많은 순으로 상위 국내도서 카테고리를 조회합니다.
     * * @param top 추출할 상위 장르의 개수
     * @param min 장르 노출을 위한 최소 도서 보유 권수 (필터링 기준)
     * @return 선별된 국내도서 부모 장르 목록
     */
    List<GenreDTO> selectParentGenresAuto(@Param("top") int top, @Param("min") int min);

    /**
     * [국내도서 하위 장르 조회]
     * 특정 부모 장르에 속한 자식 장르 목록을 조회합니다.
     * * @param parentId 조회할 부모 장르의 식별자
     * @param top 추출할 하위 장르의 최대 개수
     * @param min 최소 도서 보유 권수
     * @return 부모 장르에 소속된 자식 장르 목록
     */
    List<GenreDTO> selectChildGenresAuto(
            @Param("parentId") int parentId,
            @Param("top") int top,
            @Param("min") int min
    );

    /**
     * [외국도서 상위 장르 자동 선별]
     * 외국도서 카테고리 중 데이터 비중이 높은 상위 장르를 조회합니다.
     * * @param top 추출할 상위 장르의 개수
     * @param min 최소 도서 보유 권수
     * @return 선별된 외국도서 부모 장르 목록
     */
    List<GenreDTO> selectForeignParentGenresAuto(@Param("top") int top, @Param("min") int min);
}
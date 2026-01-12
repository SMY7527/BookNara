package com.booknara.booknaraPrj.admin.bookManagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdminGenreRepository extends JpaRepository<AdminGenre, Integer> {
    // 몰별로 장르를 가져오거나, 이름순으로 정렬해서 가져오는 기능
    List<AdminGenre> findAllByOrderByGenreNmAsc();

    // 특정 몰(국내도서 등)의 장르만 가져오기
    List<AdminGenre> findByMallOrderByGenreNmAsc(String mall);
}
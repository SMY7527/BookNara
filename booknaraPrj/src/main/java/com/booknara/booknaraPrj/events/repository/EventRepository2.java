package com.booknara.booknaraPrj.events.repository;

import com.booknara.booknaraPrj.events.dto.EventDTO;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EventRepository2 {

    private final SqlSession sqlSession;

    public EventRepository2(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    public List<EventDTO> findOngoing() {
        return sqlSession.selectList("event.selectOngoingWithMainImage");
    }

    public List<EventDTO> findClosing(int soonDays) {
        return sqlSession.selectList("event.selectClosingWithMainImage", soonDays);
    }

    public List<EventDTO> findClosed() {
        return sqlSession.selectList("event.selectClosedWithMainImage");
    }

    public List<EventDTO> findAll() {
        return sqlSession.selectList("event.selectAll2");
    }

    public EventDTO findById(String id) {
        return sqlSession.selectOne("event.selectById", id);
    }

    public int insertEvent(EventDTO dto) {
        return sqlSession.insert("event.insertEvent", dto);
    }
}

package com.booknara.booknaraPrj.events.repository;

import com.booknara.booknaraPrj.events.dto.ImageDTO;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ImageRepository {

    private final SqlSession sqlSession;

    public ImageRepository(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    // ✅ 팀 규칙: IMG_TYPE 통일
    public static final String TYPE_THUMB = "THUMB";
    public static final String TYPE_MAIN = "MAIN";
    public static final String TYPE_CONTENT = "CONTENT";

    public int insertEventImage(Long eventId, String imgUrl, String imgType) {
        Map<String, Object> param = new HashMap<>();
        param.put("eventId", eventId);
        param.put("imgUrl", imgUrl);
        param.put("imgType", imgType);

        return sqlSession.insert("image.insertEventImage", param);
    }

    public List<ImageDTO> findAll() {
        return sqlSession.selectList("image.selectAll");
    }

    // (호환용)
    public int insertEventThumb(Long eventId, String imgUrl) {
        return insertEventImage(eventId, imgUrl, TYPE_THUMB);
    }
}

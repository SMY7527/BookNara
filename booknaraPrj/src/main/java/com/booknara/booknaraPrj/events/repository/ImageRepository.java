package com.booknara.booknaraPrj.events.repository;


import com.booknara.booknaraPrj.events.dto.ImageDTO;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ImageRepository  {


      private  final  SqlSession sqlSession  ;


    public ImageRepository(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    // ✅ 이벤트 썸네일 저장
    public int insertEventThumb(Long eventId, String imgUrl) {
        java.util.Map<String, Object> param = new java.util.HashMap<>();
        param.put("targetId", eventId);
        param.put("imgUrl", imgUrl);

        return sqlSession.insert("event.insertEventThumb", param);
    }




    public List<ImageDTO> findAll(){

        return  sqlSession.selectList("image.selectAll");

    }

    public int insertEvenStThumb(Long eventId, String imgUrl) {
        java.util.Map<String, Object> param = new java.util.HashMap<>();
        param.put("targetId", eventId);
        param.put("imgUrl", imgUrl);

        // ✅ namespace가 event인 MainEventMapper.xml에 넣을 거라서 event.insertEventThumb
        return sqlSession.insert("event.insertEventThumb", param);
    }



}

package com.booknara.booknaraPrj;

import com.booknara.booknaraPrj.bookAPI.client.aladin.AladinProperties;
import com.booknara.booknaraPrj.bookAPI.client.naver.NaverProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

//@MapperScan({"com.booknara.booknaraPrj.login_signup",
//        "com.booknara.booknaraPrj.bookSearch",
//        "com.booknara.booknaraPrj.bookAPI.mapper"})
@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.booknara.booknaraPrj")
@EnableConfigurationProperties({NaverProperties.class, AladinProperties.class})
@MapperScan({"com.booknara.booknaraPrj.**.mapper",
        "com.booknara.booknaraPrj.mypage"})
//@MapperScan(basePackages = {
//        "com.booknara.booknaraPrj.login_signup.mapper",
//        "com.booknara.booknaraPrj.bookAPI.mapper",
//        "com.booknara.booknaraPrj.admin.mapper", // 필요한 만큼 추가
//        "com.booknara.booknaraPrj.bookcart.mapper",
//        "com.booknara.booknaraPrj.bookcirculation.command.mapper",
//        "com.booknara.booknaraPrj.bookcirculation.status.mapper",
//        "com.booknara.booknaraPrj.bookDetail.mapper",
//        "com.booknara.booknaraPrj.feed.review.mapper",
//        "com.booknara.booknaraPrj.reviewstatus.mapper",
//        "com.booknara.booknaraPrj.bookMark.mapper",
//        "com.booknara.booknaraPrj.bookSearch.mapper",
//        "com.booknara.booknaraPrj.mainpage.mapper",
//        "com.booknara.booknaraPrj.mypage.mylibrary",
//        "com.booknara.booknaraPrj.mypage.event",
//        "com.booknara.booknaraPrj.mypage.info",
//        "com.booknara.booknaraPrj.mypage.myinquiry",
//        "com.booknara.booknaraPrj.mypage.withdraw"
//})

public class BooknaraPrjApplication {

	public static void main(String[] args) {
		SpringApplication.run(BooknaraPrjApplication.class, args);
	}

}

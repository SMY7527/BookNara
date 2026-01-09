package com.booknara.booknaraPrj;

import com.booknara.booknaraPrj.bookAPI.client.aladin.AladinProperties;
import com.booknara.booknaraPrj.bookAPI.client.naver.NaverProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({NaverProperties.class, AladinProperties.class})
public class BooknaraPrjApplication {

	public static void main(String[] args) {
		SpringApplication.run(BooknaraPrjApplication.class, args);
	}

}

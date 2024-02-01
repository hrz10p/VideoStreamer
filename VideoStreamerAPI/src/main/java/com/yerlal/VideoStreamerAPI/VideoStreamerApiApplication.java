package com.yerlal.VideoStreamerAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class VideoStreamerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideoStreamerApiApplication.class, args);
	}

}

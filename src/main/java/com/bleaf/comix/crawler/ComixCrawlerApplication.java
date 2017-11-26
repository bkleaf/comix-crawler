package com.bleaf.comix.crawler;

import com.bleaf.comix.crawler.service.ComixCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class ComixCrawlerApplication implements CommandLineRunner {

	@Autowired
	ComixCrawler comixCrawler;

	public static void main(String[] args) {
		SpringApplication.run(ComixCrawlerApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		comixCrawler.crawlling("http://marumaru.in/c/26");
	}
}

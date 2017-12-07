package com.bleaf.comix.crawler;

import com.bleaf.comix.crawler.domain.marumaru.DailyCrawler;
import com.bleaf.comix.crawler.domain.utility.ComixUtil;
import com.bleaf.comix.crawler.service.ComixCrawlerService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.DateTimeException;
import java.util.Date;

@SpringBootApplication
public class ComixCrawlerApplication implements CommandLineRunner {

	@Autowired
	ComixCrawlerService comixCrawlerService;

	@Autowired
	ComixUtil comixUtil;

	@Autowired
	DailyCrawler dailyCrawler;

	public static void main(String[] args) {
		SpringApplication.run(ComixCrawlerApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		dailyCrawler.getDailyList(new DateTime(new Date()));
//		comixCrawlerService.crawlling("http://marumaru.in/c/26");

//		comixUtil.isShort("대장은 ef1781 항화 22화");
	}
}

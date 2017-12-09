package com.bleaf.comix.crawler;

import com.bleaf.comix.crawler.configuration.MarumaruConfig;
import com.bleaf.comix.crawler.configuration.UserAgent;
import com.bleaf.comix.crawler.domain.application.Compressor;
import com.bleaf.comix.crawler.domain.application.Downloader;
import com.bleaf.comix.crawler.domain.dto.Comix;
import com.bleaf.comix.crawler.domain.marumaru.DailyCrawler;
import com.bleaf.comix.crawler.domain.utility.ComixUtil;
import com.bleaf.comix.crawler.service.ComixCrawlerService;
import com.google.common.net.UrlEscapers;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.util.Date;
import java.util.List;

@Slf4j
@SpringBootApplication
public class ComixCrawlerApplication implements CommandLineRunner {

	@Autowired
	ComixCrawlerService comixCrawlerService;

	@Autowired
	ComixUtil comixUtil;

	@Autowired
	DailyCrawler dailyCrawler;

	@Autowired
	Downloader downloader;

	@Autowired
	Compressor compressor;

	@Autowired
	MarumaruConfig marumaruConfig;

	public static void main(String[] args) {
		SpringApplication.run(ComixCrawlerApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		DateTime dateTime = DateTimeFormat
				.forPattern("yyyyMMdd")
				.parseDateTime("20171209");
		List<Comix> list = dailyCrawler.getDailyList(dateTime);

		downloader.download(list);
		compressor.zip(list, dateTime);

		log.info("ist = {}", list.size());
//		comixCrawlerService.crawlling("http://marumaru.in/c/26");

//		comixUtil.isShort("대장은 ef1781 항화 22화");
	}
}

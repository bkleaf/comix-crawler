package com.bleaf.comix.crawler;

import com.bleaf.comix.crawler.configuration.MarumaruConfig;
import com.bleaf.comix.crawler.domain.application.Compressor;
import com.bleaf.comix.crawler.domain.application.Downloader;
import com.bleaf.comix.crawler.domain.crawler.marumaru.DateCrawler;
import com.bleaf.comix.crawler.domain.dto.Comix;
import com.bleaf.comix.crawler.domain.utility.ComixUtil;
import com.bleaf.comix.crawler.domain.utility.HtmlParserUtil;
import com.bleaf.comix.crawler.service.ComixCrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class ComixCrawlerApplication implements CommandLineRunner {

    @Autowired
    ComixCrawlerService comixCrawlerService;

    @Autowired
    ComixUtil comixUtil;

    @Autowired
    DateCrawler dateCrawler;

    @Autowired
    Downloader downloader;

    @Autowired
    Compressor compressor;

    @Autowired
    MarumaruConfig marumaruConfig;

    @Autowired
    HtmlParserUtil htmlParserUtil;

    public static void main(String[] args) {
        SpringApplication.run(ComixCrawlerApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
		DateTime dateTime = DateTimeFormat
				.forPattern("yyyyMMdd")
				.parseDateTime("20171209");

		comixCrawlerService.crawlingByDate("20171218");
//
//		downloader.download(list);
//		compressor.zip(list, dateTime);
//
//		log.info("ist = {}", list.size());

//		comixCrawlerService.crawlingByName("오크가 범해주질 않아!", "1,3-5");
    }
}

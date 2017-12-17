package com.bleaf.comix.crawler;

import com.bleaf.comix.crawler.configuration.MarumaruConfig;
import com.bleaf.comix.crawler.domain.application.Compressor;
import com.bleaf.comix.crawler.domain.application.Downloader;
import com.bleaf.comix.crawler.domain.crawler.marumaru.DateCrawler;
import com.bleaf.comix.crawler.domain.utility.ComixUtil;
import com.bleaf.comix.crawler.domain.utility.HtmlParserUtil;
import com.bleaf.comix.crawler.service.ComixCrawlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.regex.Pattern;

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
//		DateTime dateTime = DateTimeFormat
//				.forPattern("yyyyMMdd")
//				.parseDateTime("20171209");
//		List<Comix> list = dailyCrawler.getDailyList(dateTime);
//
//		downloader.download(list);
//		compressor.zip(list, dateTime);
//
//		log.info("ist = {}", list.size());

//		comixCrawlerService.crawlingByName("오크가 범해주질 않아!", "1,3-5");

        String[] titles = {"성녀의 마력은 만능입니다 4-3, 4-4화"
                , "이종족 리뷰어즈 01~13화"
                , "메이저 2부 97화"
                , "소꿉친구와, 키스하고 싶지 않아. 10화"
                , "역전! 러브게임"
                , "장난을 잘치는 (전) 타카기 씨 22화"
                , "날조트랩 -NTR- 26화 (완결)"
                , "토바쿠 선배 뭘 걸래? 13화"
                , "Stand Up! 6화"
                , "카케구루이(임시) 22화"};


        for(String t : titles) {
            String episode = comixUtil.getEpisode(t);
            String cn = comixUtil.getComixName(t);
            log.info("episode = {} : {}", cn, episode);
        }

        /*
         * title 4화, title 4화 전편 title 4.5화, title 4-3, 4-4화, title 전편, title 후편, title 4-7화
         * title 11~13화, title 20, 21화
         */



//        // 4화
//        Pattern pattern1 = Pattern.compile("(^[0-9])([\\.\\-\\~][0-9])?[화권]+");
//
//        // 4-1, 4-2, 4-3, 4-4화
//
//        Pattern pattern2 = Pattern.compile("(^[0-9])([\\.\\-\\~][0-9])?\\,");
//
//        String title = "4~5화";
//
//        if(Pattern.matches("(^[0-9])([\\.\\-\\~][0-9])?[화권]+", title)) {
//            log.info("aaaaa = {}", title);
//        } else if(pattern2.matcher(title).find()){
//            log.info("bbbb = {}", title);
//        } else {
//            log.info("fail = {}", title);
//        }
	}
}

package com.bleaf.comix.crawler.service;

import com.bleaf.comix.crawler.domain.application.Compressor;
import com.bleaf.comix.crawler.domain.application.Downloader;
import com.bleaf.comix.crawler.domain.dto.Comix;
import com.bleaf.comix.crawler.domain.marumaru.DateCrawler;
import com.bleaf.comix.crawler.domain.marumaru.TitleCrawler;
import com.bleaf.comix.crawler.domain.utility.ComixUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ComixCrawlerService {

    @Autowired
    ComixUtil comixUtil;

    @Autowired
    DateCrawler dateCrawler;

    @Autowired
    TitleCrawler titleCrawler;

    @Autowired
    Downloader downloader;

    @Autowired
    Compressor compressor;

    @Scheduled(cron = "0, 0, 2, *, *, ?")
    public void crawlingByDate() {
        String date = new DateTime().toString("yyyyMMdd");
        this.crawlingByDate(date);
    }

    public void crawlingByDate(String date) {
        DateTime today = DateTimeFormat
                .forPattern("yyyyMMdd")
                .parseDateTime(date);

        log.info(" ### start crawling = {} : {}",  today, today.toString("yyyyMMdd"));

        // download 받을 기본 directory 생성
        // home directory + 날짜(yyyyMMdd)
        try {
            if (!comixUtil.makeDownloadDirectory(date)) {
                log.error("download date 디렉토리 또는 service date 디렉토리 생성 실패 = {}", date);
                log.info(" ### daily crawling이 중단 되었습니다 = {}", date);

                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Comix> comixList = dateCrawler.getComixList(today);

        if(comixList == null || comixList.isEmpty()) {
            log.error(" ### update comix list is null or size 0 = {}", today.toString("yyyyMMdd"));
        }
        log.info(" ### complete update comix list = {}", comixList.size());

        int count = downloader.download(comixList);
        log.info(" ### complete download = {} : {}", comixList.size(), count);

        compressor.zip(comixList);
        log.info(" ### complete compress");
    }

    public void crawlingByName(String comixName) {
        this.crawlingByName(comixName, null);
    }

    public void crawlingByName(String comixName, String range) {
        log.info(" ### start crawling = {}", comixName);

        try {
            // 디렉토리 생성시에는 금지 문자를 체크 하여 생성한다
            // window와 linux 쪽의 금지 문자가 다르다
            if (!comixUtil.makeDownloadDirectory(comixUtil.checkTitle(comixName))) {
                log.error("download date 디렉토리 또는 service comix 디렉토리 생성 실패 = {}", comixName);
                log.info(" ### title crawling이 중단 되었습니다 = {}", comixName);

                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Comix> comixList = titleCrawler.getComixList(comixName, range);

        if(comixList == null || comixList.isEmpty()) {
            log.error(" ### comix list is null or size 0 = {}", comixName);
        }

        log.info(" ### complete comix list = {}", comixList.size());

        int count = downloader.download(comixList);
        log.info(" ### complete download = {} : {}", comixList.size(), count);

        compressor.zip(comixList);
        log.info(" ### complete compress");
    }
}

package com.bleaf.comix.crawler.service;

import com.bleaf.comix.crawler.configuration.UserAgent;
import com.bleaf.comix.crawler.domain.application.Compressor;
import com.bleaf.comix.crawler.domain.application.Downloader;
import com.bleaf.comix.crawler.domain.dto.Comix;
import com.bleaf.comix.crawler.domain.marumaru.DailyCrawler;
import com.bleaf.comix.crawler.domain.marumaru.TitleCrawler;
import com.bleaf.comix.crawler.domain.utility.ComixUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ComixCrawlerService {

    @Autowired
    ComixUtil comixUtil;

    @Autowired
    DailyCrawler dailyCrawler;

    @Autowired
    TitleCrawler titleCrawler;

    @Autowired
    Downloader downloader;

    @Autowired
    Compressor compressor;


    public void dailyCrawling() {

        String date = new DateTime().toString("yyyyMMdd");

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

        List<Comix> comixList = dailyCrawler.getComixList(today);

        if(comixList == null || comixList.isEmpty()) {
            log.error(" ### update comix list is null or size 0 = {}", today.toString("yyyyMMdd"));
        }
        log.info(" ### complete update comix list = {}", comixList.size());

        int count = downloader.download(comixList);
        log.info(" ### complete download = {} : {}", comixList.size(), count);

        compressor.zip(comixList, today);
        log.info(" ### complete compress");
    }

    public void titleCrawling(String comixName) {
        log.info(" ### start crawling = {}", comixName);

        titleCrawler.getComixList(comixName);

        try {
            if (!comixUtil.makeDownloadDirectory(comixName)) {
                log.error("download date 디렉토리 또는 service date 디렉토리 생성 실패 = {}", date);
                log.info(" ### daily crawling이 중단 되었습니다 = {}", date);

                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Comix> comixList = dailyCrawler.getComixList(today);

        if(comixList == null || comixList.isEmpty()) {
            log.error(" ### update comix list is null or size 0 = {}", today.toString("yyyyMMdd"));
        }
        log.info(" ### complete update comix list = {}", comixList.size());

        int count = downloader.download(comixList);
        log.info(" ### complete download = {} : {}", comixList.size(), count);

        compressor.zip(comixList, today);
        log.info(" ### complete compress");
    }
}

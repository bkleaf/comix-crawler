package com.bleaf.comix.crawler.presentation;

import com.bleaf.comix.crawler.domain.json.DownloadResult;
import com.bleaf.comix.crawler.service.ComixCrawlerService;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping(value="/comix")
public class CrawlerController {
    @Autowired
    ComixCrawlerService comixCrawlerService;

    @RequestMapping("/date/{date}")
    @ResponseBody
    public DownloadResult downloadByDate(@PathVariable String date) {
        String strDate = "" + date;

        if(Strings.isNullOrEmpty(strDate) || strDate.length() != 8) return null;
        log.info("download date = {}", strDate);

        comixCrawlerService.crawlingByDate(strDate);

        return new DownloadResult();
    }

    @RequestMapping("/name/{name}")
    @ResponseBody
    public DownloadResult downloadByName(@PathVariable String name) {
        if(Strings.isNullOrEmpty(name)) return null;
        log.info("download comix name = {}", name);

        comixCrawlerService.crawlingByName(name);

        return new DownloadResult();
    }

    @RequestMapping("/range/{range}/{name}")
    @ResponseBody
    public DownloadResult downloadByName(@PathVariable String range,
                                         @PathVariable String name) {
        if(Strings.isNullOrEmpty(name)) return null;
        log.info("download comix name, range = {} : {}", name, range);

        comixCrawlerService.crawlingByName(name, range);

        return new DownloadResult();
    }
}

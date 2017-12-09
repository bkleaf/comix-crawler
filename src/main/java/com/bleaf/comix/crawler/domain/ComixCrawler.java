package com.bleaf.comix.crawler.domain;

import com.bleaf.comix.crawler.domain.dto.Comix;
import org.joda.time.DateTime;

import java.util.List;

public interface ComixCrawler {
    List<Comix> getComixList(DateTime today);
    List<Comix> getComixList(String comixName);
}

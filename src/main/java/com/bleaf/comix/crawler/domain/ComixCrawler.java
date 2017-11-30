package com.bleaf.comix.crawler.domain;

import org.joda.time.DateTime;

import java.util.List;

public interface ComixCrawler {
    List<String> getComixLIst();
    List<String> getComixList(DateTime dt);
    // title 검색시 title에 해당하는 각 권의 url을 수집한다
    List<String> getVolumeList();
    // 각 권의 이미지 url을 수집한다.
    List<String> getPageList();

}

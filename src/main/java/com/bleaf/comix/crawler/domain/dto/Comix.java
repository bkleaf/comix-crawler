package com.bleaf.comix.crawler.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.nio.file.Path;
import java.util.List;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comix {
    // comic name
    // ex) 원피스
    String name;

    // 다운로드 받는 페이지의 title
    // ex) 원피스 141화
    String title;
    DateTime updateDate;
    boolean shortComix;
    String volumn;
    List<String> exts;

//    Path homePath;
    Path downloadPath;
    Path servicePath;

    // 전편 보기와, 한회 보기가 있는 page url;
    String comixUri;
    // 전편 page url;
    String allSeriesUri;
    // 한 회의 만화가 있는 page url;
    String oneVolumnUri;
    // 만화 img uri
    List<String> imageUris;
}

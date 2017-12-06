package com.bleaf.comix.crawler.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.nio.file.Path;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comix {
    String title;
    DateTime updateDate;
    boolean shortComix;
    // 전편 보기와, 한회 보기가 있는 page url;
    String comixUri;
    // 전편 page url;
    String allSeriesUri;
    // 한 회의 만화가 있는 page url;
    String oneVolumnUri;
    String volumn;
}

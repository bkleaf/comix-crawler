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
    Path allList;
    Path oneList;
}

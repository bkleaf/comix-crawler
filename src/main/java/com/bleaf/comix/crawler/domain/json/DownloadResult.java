package com.bleaf.comix.crawler.domain.json;

import lombok.Data;

import java.util.List;

@Data
public class DownloadResult {
    String date;
    List<String> downloadList;

    int downloadCount;
    int compressCount;
}

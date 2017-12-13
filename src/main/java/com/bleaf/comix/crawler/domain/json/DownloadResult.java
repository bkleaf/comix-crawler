package com.bleaf.comix.crawler.domain.json;

import lombok.Data;

import java.util.List;

@Data
public class DownloadResult {
    List<String> downloadList;
}

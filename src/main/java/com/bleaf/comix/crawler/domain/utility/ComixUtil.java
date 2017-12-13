package com.bleaf.comix.crawler.domain.utility;

import com.bleaf.comix.crawler.configuration.ComixConfig;
import com.bleaf.comix.crawler.configuration.MarumaruConfig;
import com.bleaf.comix.crawler.configuration.UserAgent;
import com.bleaf.comix.crawler.domain.dto.Comix;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.google.common.base.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ComixUtil {
    @Autowired
    ComixConfig comixConfig;

    @Autowired
    MarumaruConfig marumaruConfig;

    public String getComixName(String title) {
        if (!title.endsWith("화") && !title.endsWith("권")) return title;

        List<String> titles = Splitter.on(" ")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(title);

        String lastChar = titles.get((titles.size() - 1));
        String page = lastChar.replaceAll("[a-z|A-Z|ㄱ-ㅎ|가-힣]", "");

        if (Strings.isNullOrEmpty(page)) {
            return title;
        }

        String name = "";
        String whiteSpace = "";

        int size = titles.size() - 1;
        for(int i=0; i < size; i++) {
            name += whiteSpace + titles.get(i);
            whiteSpace = " ";
        }

        return name;
    }

    public boolean isImageUrl(String uri) {
        URL url = null;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            log.debug("정상 적인 URL이 아닙니다 = {}", uri);
            return false;
        }

        if(url == null) {
            return false;
        }

        String host = url.getHost();


        List<String> domains = comixConfig.getImageFileDomains();
        for(String domain : domains) {
            if(CharMatcher.anyOf(domain).matchesAllOf(host)) {
                return true;
            }
        }

        return false;
    }

    public boolean isShort(String title) {
        Preconditions.checkNotNull(title, "title is null");

        String episode = this.getEpisode(title);

        return Strings.isNullOrEmpty(episode);
    }

    public String getEpisode(String title) {
        String episode = null;
        if(!title.endsWith("화") && !title.endsWith("권")) return episode;

        List<String> titles = Splitter.on(" ")
                .omitEmptyStrings()
                .splitToList(title);

        String lastChar = titles.get((titles.size() - 1));
        episode = lastChar.replaceAll("[a-z|A-Z|ㄱ-ㅎ|가-힣]", "");

        log.debug("last char = {} : convert page = {}", lastChar, episode);

        return episode;
    }

    public boolean makeDownloadDirectory(String directoryName) throws IOException {
        Path basePath = Paths.get(comixConfig.getBasePath());
        Path servicePath = Paths.get(comixConfig.getServicePath());

        if(!Files.exists(basePath))
            throw new RuntimeException("comix 다운로드 기본 디렉토리가 없습니다 = " + basePath.toAbsolutePath());

        if(!Files.exists(servicePath))
            throw new RuntimeException("comix 서비스 기본 디렉토리가 없습니다 = " + servicePath.toAbsolutePath());

        Path downloadDatePath = basePath.resolve(directoryName);
        Path serviceDatePath = servicePath.resolve(directoryName);

        if(!Files.exists(downloadDatePath)) {
            Files.createDirectory(downloadDatePath);
        } else {
            log.info("다운로드 디렉토리가 이미 존재 합니다 = {}", directoryName);
        }

        if(!Files.exists(serviceDatePath)) {
            Files.createDirectory(serviceDatePath);
        } else {
            log.info("서비스 디렉토리가 이미 존재 합니다 = {}", directoryName);
        }

        return (Files.exists(downloadDatePath) && Files.exists(serviceDatePath));
    }

    public boolean makeComixDirectory(Comix comix) throws IOException {
        try {
            log.debug("comix {} 디렉토리 생성합니다", comix.getTitle());
            Path comixPath = comix.getDownloadPath();

            if (Files.exists(comixPath)) {
                log.info("해당 디렉토리가 존재 합니다");
                FileSystemUtils.deleteRecursively(comixPath.toFile());
            }

            log.debug("make comix direcotry = {}", comix.getTitle());
            Files.createDirectory(comixPath);
        } catch (Exception e) {
            log.error("fail make directory = {}", e.getMessage());
        }

        return Files.exists(comix.getDownloadPath());
    }

    public List<String> getImageExtension(List<String> comixPages) {
        List<String> exts = Lists.newArrayList();

        String ext;
        for(String page : comixPages) {
            ext = com.google.common.io.Files.getFileExtension(page);
            log.debug("comix page file extension = {} : {}", ext, page);

            if(!exts.contains(ext)) {
                exts.add(ext);
            }
        }

        return exts;
    }

    public List<String> getEpisodeRange(String range) {
        if(range == null) return null;

        List<String> arrRange = Splitter.on(",")
                .trimResults()
                .omitEmptyStrings()
                .splitToList(range);

        List<String> episodeList = Lists.newArrayList();
        for(String episode : arrRange) {
            if(isNum(episode)) {
                episodeList.add(episode);
            } else {
                if(!CharMatcher.is('-').matchesAllOf(episode)) continue;

                List<String> list = Splitter.on("-")
                        .trimResults()
                        .splitToList(episode);

                if(list.size() != 2) continue;;

                try {
                    int startIdx = Integer.parseInt(list.get(0));
                    int endIdx = Integer.parseInt(list.get(1));

                    for (int i = startIdx; i <= endIdx; i++) {
                        episodeList.add("" + i);
                    }
                }catch (NumberFormatException e) {
                    log.error("다운 로드 범위 지정이 잘못 되었습니다 = {} - {}", list.get(0), list.get(1));
                    continue;
                }
            }
        }

        return episodeList;
    }

    public String checkTitle(String title) {
        title = title.replaceAll("[\\/:*?<>|.]", " ").trim();
        title = title.replaceAll("^\\[[ㄱ-ㅎ|가-힣]\\]+", "").trim();

        return title;
    }

    private boolean isNum(String page) {
        try {
            Double.parseDouble(page);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}

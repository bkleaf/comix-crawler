package com.bleaf.comix.crawler.domain.utility;

import com.bleaf.comix.crawler.configuration.ComixConfig;
import com.bleaf.comix.crawler.configuration.MarumaruConfig;
import com.bleaf.comix.crawler.domain.dto.Comix;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ComixUtil {
    // 4화, 4.5화, 4-5화, 4~6화
    private static final String COMIX_NANE_PATTERN1 = "(^[0-9]{1,})([.\\-~][0-9]{1,})?[화권]+";
    // 4-1, 4, 4~7,
    private static final String COMIX_NANE_PATTERN2 = "(^[0-9]{1,})([.\\-~][0-9]{1,})?,";
    // 2부 73화
    private static final String COMIX_NAME_PATTERN3 = "(^[0-9]{1,}부)";

    @Autowired
    ComixConfig comixConfig;

    @Autowired
    MarumaruConfig marumaruConfig;

    /*
     * title 4화, title 4화 전편 title 4.5화, title 4-3, 4-4화, title 전편, title 후편, title 4-7화
     * title 11~13화, title 20, 21화
     * title 2부 73화
     */
    public String getComixName(String title) {
        List<String> titles = Splitter.on(" ")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(title);

        String word;
        int lastIdx = 0;

        String comixName = "";
        String whiteSpace = "";
        for (int i = 0; i < titles.size(); i++) {
            word = titles.get(i);
            if (Pattern.matches(COMIX_NANE_PATTERN1, word)) {
                break;
            }

            if (Pattern.matches(COMIX_NANE_PATTERN2, word)) {
                if(isEpisode(titles, i)) {
                    break;
                }
            }

            comixName += whiteSpace + word;
            whiteSpace = " ";
        }

        return comixName;
    }

    private boolean isEpisode(List<String> titles, int idx) {
        String word;
        for(int i = idx; i < titles.size(); i++) {
            word = titles.get(i);

            if(Pattern.matches(COMIX_NANE_PATTERN1, word)) {
                return true;
            }

            if(!Pattern.matches(COMIX_NANE_PATTERN2, word)) {
                return false;
            }
        }

        return false;
    }

    public boolean isImageUrl(String uri) {
        String domainName = null;
        try {
            domainName = this.getDomainName(new URL(uri).getHost());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (domainName != null && comixConfig.getImageFileDomains().contains(domainName.toLowerCase())) {
            return true;
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

        List<String> titles = Splitter.on(" ")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(title);

        String str = null;
        int lastIdx = 0;

        for (int i = 0; i < titles.size(); i++) {
            str = titles.get(i);
            if (str.endsWith("화") || str.endsWith("권")) {
                break;
            }
        }

        if (str == null) {
            return episode;
        }

        episode = str.replaceAll("[a-z|A-Z|ㄱ-ㅎ|가-힣]", "");

        log.debug("last char = {} : convert page = {}", str, episode);

        return episode;
    }

    public boolean makeDownloadDirectory(String directoryName) throws IOException {
        Path basePath = Paths.get(comixConfig.getDownloadPath());
        Path servicePath = Paths.get(comixConfig.getServicePath());

        if (!Files.exists(basePath))
            throw new RuntimeException("comix 다운로드 기본 디렉토리가 없습니다 = " + basePath.toAbsolutePath());

        if (!Files.exists(servicePath))
            throw new RuntimeException("comix 서비스 기본 디렉토리가 없습니다 = " + servicePath.toAbsolutePath());

        Path downloadDatePath = basePath.resolve(directoryName);
        Path serviceDatePath = servicePath.resolve(directoryName);

        if (!Files.exists(downloadDatePath)) {
            Files.createDirectory(downloadDatePath);
        } else {
            log.info("다운로드 디렉토리가 이미 존재 합니다 = {}", directoryName);
        }

        if (!Files.exists(serviceDatePath)) {
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
        for (String page : comixPages) {
            ext = com.google.common.io.Files.getFileExtension(page);
            log.debug("comix page file extension = {} : {}", ext, page);

            if (!exts.contains(ext)) {
                exts.add(ext);
            }
        }

        return exts;
    }

    public List<String> getEpisodeRange(String range) {
        if (range == null) return null;

        List<String> arrRange = Splitter.on(",")
                .trimResults()
                .omitEmptyStrings()
                .splitToList(range);

        List<String> episodeList = Lists.newArrayList();
        for (String episode : arrRange) {
            if (isNum(episode)) {
                episodeList.add(episode);
            } else {
                if (!CharMatcher.is('-').matchesAnyOf(episode)) continue;

                List<String> list = Splitter.on("-")
                        .trimResults()
                        .splitToList(episode);

                if (list.size() != 2) continue;

                try {
                    int startIdx = Integer.parseInt(list.get(0));
                    int endIdx = Integer.parseInt(list.get(1));

                    for (int i = startIdx; i <= endIdx; i++) {
                        episodeList.add("" + i);
                    }
                } catch (NumberFormatException e) {
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

    public String getDomainName(String host) {
        Preconditions.checkNotNull(host, "host name is null");

        log.debug("host name = {}", host);
        List<String> hosts = Splitter.on(".").trimResults().splitToList(host);

        if (hosts.size() == 1) {
            return null;
        }

        return hosts.get(hosts.size() - 2);
    }

    public boolean isNum(String page) {
        try {
            Double.parseDouble(page);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}

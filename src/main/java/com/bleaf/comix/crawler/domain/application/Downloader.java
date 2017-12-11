package com.bleaf.comix.crawler.domain.application;

import com.bleaf.comix.crawler.configuration.ComixConfig;
import com.bleaf.comix.crawler.configuration.UserAgent;
import com.bleaf.comix.crawler.domain.dto.Comix;
import com.bleaf.comix.crawler.domain.utility.ComixUtil;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class Downloader {

    @Autowired
    ComixConfig comixConfig;

    @Autowired
    ComixUtil comixUtil;

    public int download(List<Comix> comixList) {
        log.info(" ### start download comix list = {}", comixList.size());

        int count=0;
        List<Comix> retryList = Lists.newArrayList();
        for (Comix comix : comixList) {
            try {
                this.download(comix);
                count += 1;
            } catch (Exception e) {
                log.error("### error = {}", e.getMessage());

                retryList.add(comix);
            }
        }

        if(retryList.size() == 0) {
            return count;
        }

        log.info(" ### 다운로드 실패한 항목에 대해 재시도를 합니다. = {}", retryList.size());

        for(Comix comix : retryList) {
            try {
                this.download(comix);
                count += 1;
            } catch (Exception e) {
                log.error("### error = {}", e.getMessage());
            }
        }

        return count;
    }

    public void download(Comix comix) throws IOException {
        log.info("start download comix = {}", comix.getTitle());

        Preconditions.checkNotNull(comix, "comix is null");
        Preconditions.checkArgument((comix.getImageUris() != null && !comix.getImageUris().isEmpty())
                , "image url list is null");

        if(!comixUtil.makeComixDirectory(comix)) {
            log.error("fail comix download = {}", comix);
            throw new RuntimeException(
                    "comix 디렉토리 생성을 못해 다운로드에 실패했습니다 = " + comix.getTitle());
        }

        List<String> imageUrls = comix.getImageUris();

        Path imagePath;

        int page = 1;
        String ext;

        Stopwatch sw;

        HttpURLConnection httpURLConnection;
        for (String imgUrl : imageUrls) {
            sw = Stopwatch.createStarted();
            ext = com.google.common.io.Files.getFileExtension(imgUrl);

            log.debug("get ext = {}", sw.elapsed(TimeUnit.MILLISECONDS));

            if (Strings.isNullOrEmpty(ext)) {
                ext = "jpg";
            }

            sw = Stopwatch.createStarted();
            imagePath = comix.getDownloadPath().resolve("b_comix_" + String.format("%03d", page) + "." + ext);

            log.debug("get imagePath = {}", sw.elapsed(TimeUnit.MILLISECONDS));

            log.debug("download image path = {} -> {}", imgUrl, imagePath);


            sw = Stopwatch.createStarted();
            httpURLConnection = (HttpURLConnection) new URL(imgUrl).openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setRequestProperty("charset", "utf-8");
            httpURLConnection.setRequestProperty("User-Agent", UserAgent.getUserAgent());
            httpURLConnection.setRequestProperty("Accept-Encoding", "gzip");

            try (InputStream inputStream = httpURLConnection.getInputStream()) {
                log.debug("get input stream = {}", sw.elapsed(TimeUnit.MILLISECONDS));

                sw = Stopwatch.createStarted();
                Files.copy(inputStream, imagePath);
                log.debug("write image = {}", sw.elapsed(TimeUnit.MILLISECONDS));
            } catch (IOException e) {
                e.printStackTrace();
            }

            page += 1;
        }

        log.info("comix 다운 로드 성공 = {} : {}", comix.getTitle(), page);
    }
}

package com.bleaf.comix.crawler.domain.application;

import com.bleaf.comix.crawler.configuration.ComixConfig;
import com.bleaf.comix.crawler.configuration.UserAgent;
import com.bleaf.comix.crawler.domain.dto.Comix;
import com.bleaf.comix.crawler.domain.utility.ComixUtil;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class Downloader {

    @Autowired
    ComixConfig comixConfig;

    @Autowired
    ComixUtil comixUtil;

    public void download(List<Comix> comixList) {
        log.info("start download comix list = {}", comixList.size());


        String date = new DateTime(new Date()).toString("yyyyMMdd");

        try {
            comixUtil.makeDateDirectory(date);

            for(Comix comix : comixList) {
                this.download(comix);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    public void download(Comix comix) throws IOException {
        log.info("start download comix = {}", comix.getTitle());

        Preconditions.checkNotNull(comix, "comix is null");
        Preconditions.checkArgument((comix.getImageUris() != null && !comix.getImageUris().isEmpty())
                , "image url list is null");


        Path comixPath = comixUtil.makeComixDirectory(comix);
        if(comixPath == null || !Files.exists(comixPath)) {
            log.error("fail comix download = {}", comix);
            throw new RuntimeException("comix 디렉토리 생성을 못해 다운로드에 실패했습니다 = " + comix.getTitle());
        }

        List<String> imageUrls = comix.getImageUris();

        Path imagePath;

        int page = 1;
        String ext;

        HttpURLConnection httpURLConnection = null;
        for(String imgUrl : imageUrls) {
            ext = com.google.common.io.Files.getFileExtension(imgUrl);

            if(Strings.isNullOrEmpty(ext)) {
                ext = "jpg";
            }

            imagePath = comixPath.resolve(String.format("%03d", page) + "." + ext);
            log.debug("download image path = {} -> {}", imgUrl, imagePath);

            httpURLConnection = (HttpURLConnection) new URL(imgUrl).openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setRequestProperty("charset", "utf-8");
            httpURLConnection.setRequestProperty("User-Agent", UserAgent.getUserAgent());
            httpURLConnection.setRequestProperty("Accept-Encoding", "gzip");

            try(InputStream inputStream = httpURLConnection.getInputStream()) {
                Files.copy(inputStream, imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            page += 1;
        }

        log.info("comix 다운 로드 성공 = {} : {}", comix.getTitle(), page);
    }


//    public static void main(String[] args) {
//
//        String imageUrl = "http://via.placeholder.com/350x150";
//        String destinationFilePath = "/path/to/file/test.jpg"; // For windows something like c:\\path\to\file\test.jpg
//
//        InputStream inputStream = null;
//        try {
//            inputStream = new URL(imageUrl).openStream();
//            Files.copy(inputStream, Paths.get(destinationFilePath));
//        } catch (IOException e) {
//            System.out.println("Exception Occurred " + e);
//        } finally {
//            if (inputStream != null) {
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    // Ignore
//                }
//            }
//        }
//
//    }
}

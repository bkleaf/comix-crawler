package com.bleaf.comix.crawler.domain.marumaru;

import com.bleaf.comix.crawler.configuration.ComixUrlConfig;
import com.bleaf.comix.crawler.domain.ComixCrawler;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class DailyCrawler implements ComixCrawler {
    @Autowired
    ComixUrlConfig comixUrlConfig;


    @Override
    public List<String> getComixLIst() {
        return null;
    }

    @Override
    public List<String> getComixList(DateTime dt) {
        return null;
    }

    @Override
    public List<String> getVolumeList() {
        return null;
    }

    @Override
    public List<String> getPageList() {
        return null;
    }

//    public class DownloadImage {
//        public static void main(String[] args) {
//
//            String imageUrl = "http://via.placeholder.com/350x150";
//            String destinationFilePath = "/path/to/file/test.jpg"; // For windows something like c:\\path\to\file\test.jpg
//
//            InputStream inputStream = null;
//            try {
//                inputStream = new URL(imageUrl).openStream();
//                Files.copy(inputStream, Paths.get(destinationFilePath));
//            } catch (IOException e) {
//                System.out.println("Exception Occurred " + e);
//            } finally {
//                if (inputStream != null) {
//                    try {
//                        inputStream.close();
//                    } catch (IOException e) {
//                        // Ignore
//                    }
//                }
//            }
//
//        }
//    }
}

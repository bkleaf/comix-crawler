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
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Component
public class ComixUtil {
    @Autowired
    ComixConfig comixConfig;

    @Autowired
    MarumaruConfig marumaruConfig;

    public boolean isShort(String title) {
        Preconditions.checkNotNull(title, "title is null");

        String page = this.getVolumn(title);

        return Strings.isNullOrEmpty(page);
    }

    public String getVolumn(String title) {
        String page = null;
        if(!title.endsWith("화")) return page;

        List<String> titles = Splitter.on(" ")
                .omitEmptyStrings()
                .splitToList(title);

        String lastChar = titles.get((titles.size() - 1));
        page = lastChar.replaceAll("[a-z|A-Z|ㄱ-ㅎ|가-힣]", "");

        log.debug("last char = {} : convert page = {}", lastChar, page);

        return page;
    }

    public boolean makeDownloadDirectory(String directoryName) throws IOException {
        Path basePath = Paths.get(comixConfig.getBasePath());
        Path servicePath = Paths.get(comixConfig.getServicePath());

        if(!Files.exists(basePath))
            throw new RuntimeException("comix 다운로드 기본 디렉토리가 없습니다 = " + basePath.toAbsolutePath());

        if(!Files.exists(servicePath))
            throw new RuntimeException("comix 서비스 기본 디렉토리가 없습니다 = " + servicePath.toAbsolutePath());

        Path downloadDatePath = servicePath.resolve(directoryName);
        Path serviceDatePath = servicePath.resolve(directoryName);

        if(!Files.exists(downloadDatePath)) {
            Files.createDirectory(downloadDatePath);
        } else {
            log.info("다운로드 디렉토리가 이미 존재 합니다 = {}", directoryName);
        }

        if(!Files.exists(serviceDatePath)) {
            Files.createDirectory(downloadDatePath);
        } else {
            log.info("서비스 디렉토리가 이미 존재 합니다 = {}", directoryName);
        }

        return (Files.exists(downloadDatePath) && Files.exists(serviceDatePath));
    }

    public Path makeComixDirectory(Comix comix) throws IOException {
        String title = comix.getTitle();
        String date = comix.getUpdateDate().toString("yyyyMMdd");

        Path basePath = Paths.get(comixConfig.getBasePath() + File.separator + date);

        if(!Files.exists(basePath)) {
            if(!this.makeDownloadDirectory(date)) {
                throw new RuntimeException("날짜 디렉토리 생성에 실패했습니다 = " + date);
            }
        }

        Path comixPath = null;
        try {
            log.debug("comix {} 디렉토리 생성합니다", comix.getTitle());
            comixPath = basePath.resolve(comix.getTitle());

            if (Files.exists(comixPath)) {
                log.info("해당 디렉토리가 존재 합니다");
                FileSystemUtils.deleteRecursively(comixPath.toFile());
            }

            log.debug("make comix direcotry = {}", title);
            Files.createDirectory(comixPath);
        } catch (Exception e) {
            log.error("fail make directory = {}", e.getMessage());
        }

        return comixPath;
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

    public String checkTitle(String title) {
        return title.replaceAll("[\\/:*?<>|.]", " ").trim();
    }

    public Document getHtmlPageJsoup(String listUrl, StoreType storeType){
        String pageSource = null;

        String password = "";
        if(storeType == StoreType.MARUMARU) {
            password = marumaruConfig.getPassword();
        }

        Document preDoc = null;
        try {
            preDoc = Jsoup.connect(listUrl)
                    .userAgent(UserAgent.getUserAgent())
                    .header("charset", "utf-8")
                    .header("Accept-Encoding", "gzip") //20171126 gzip 추가
                    .data("pass", password)
                    .timeout(5000)
                    .get();

        }
        catch (Exception e) {
            log.error("error html parsing = {} : {}", listUrl, e.getMessage());
        }

        return preDoc;
    }

    private String getHtmlPageHtmlUnit(String listUrl, StoreType storeType){
        String pageSource = null;

        WebClient webClient = new WebClient();
        webClient.getOptions().setRedirectEnabled(true);

        try{
            WebRequest req = new WebRequest(new URL(listUrl));
            req.setAdditionalHeader("User-Agent", UserAgent.getUserAgent());
            req.setAdditionalHeader("Accept-Encoding", "gzip");
            req.setHttpMethod(HttpMethod.POST);

            if(storeType == StoreType.MARUMARU) {
                req.getRequestParameters().add(new NameValuePair("pass", marumaruConfig.getPassword()));
            }

            HtmlPage page = webClient.getPage(req);
            pageSource = page.asXml();
        }
        catch(Exception e){
            log.error("error parsing htmlunit = {} : {}", listUrl, e.getMessage());
        }
        finally{
            webClient.close();
        }
        return pageSource;
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

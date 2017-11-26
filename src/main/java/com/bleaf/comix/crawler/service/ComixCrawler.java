package com.bleaf.comix.crawler.service;

import com.bleaf.comix.crawler.util.UserAgent;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ComixCrawler {
    private final static String ROOT_URL = "http://marumaru.in";

    public void crawlling(String url) throws IOException {
        Document rawData = Jsoup.connect(url)
                .timeout(5000)
                .get();

        Elements articles = rawData.select("tr:not(.tr_notice) a"); // 공지사항을 제외한 tr의 a 태그들을 얻어온다.

        for(Element article : articles) {

            String href = article.attr("abs:href"); // a태그 href의 절대주소를 얻어낸다.

            // a 태그 안에 포함된 div들
            Elements articleDiv = article.select("div");

            String thumbUrl = ROOT_URL
                    + articleDiv.first() // 첫 번째 div에서 썸네일 url을 얻어온다.
                    .attr("style")
                    .replace("background-image:url(", "")
                    .replace(")", "");

            String title = articleDiv.get(1).ownText(); // 두 번째 div에서 제목을 얻어낸다.

            String date = articleDiv.get(1).select("small").text()
                    .split("\\|")[0];

            log.info("title = {} : {}", title, date);
            log.info("comix url = {}", href);
            log.info("comix thumb url = {}", thumbUrl);

            this.open2(href);
        }
    }

    public void open2(String url) throws IOException {
        Document rawData = Jsoup.connect(url)
                .timeout(5000)
                .get();



        Elements contentATags = rawData.select("#vContent a"); // 공지사항을 제외한 tr의 a 태그들을 얻어온다.

        String viewPageUrl = contentATags.first()
                .attr("abs:href"); // 마찬가지로 절대주소 href를 얻어낸다

        log.info("comix info url = {}", viewPageUrl);

        this.openComixPage(viewPageUrl, url);
    }

    public void openComixPage(String url, String referer) throws IOException {
        String password = "qndxkr";

        Connection.Response response = Jsoup.connect(url)
                .userAgent(UserAgent.getUserAgent())
                .header("charset", "utf-8")
                .data("pass", password)
                .followRedirects(true)
                .execute();
        Document rawData = response.parse(); //받아온 HTML 코드를 저장




        Elements imgs = rawData.select("img[class=lz-lazyload]"); // lz-lazyload 클래스를 가진 img들

        List<String> imageUrls = new ArrayList<>();

        for(Element img : imgs) {
            imageUrls.add(img.attr("abs:data-src"));
        }

        System.out.println(imageUrls); // 이미지 URL들.
    }

    public Map<String, String> getCookies(String url, String referer) throws IOException {
        String password = "qndxkr";

        Connection.Response loginPageResponse = Jsoup.connect(url)
                .timeout(3000)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.3")
                .header("Referer", referer)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Cache-Control", "max-age=0")
                .method(Connection.Method.GET)
                .execute();

        Map<String, String> loginTryCookie = loginPageResponse.cookies();

        Map<String, String> data = Maps.newHashMap();
        data.put("pass", password);
//        data.put("")


        Connection.Response response = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.3")
                .timeout(3000)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Cache-Control", "max-age=0")
                .header("Referer", referer)
                .cookies(loginTryCookie)
                .data(data)
                .method(Connection.Method.POST)
                .execute();

        Map<String, String> loginCookie = response.cookies();

        return loginCookie;
    }
}

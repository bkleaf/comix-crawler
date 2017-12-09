package com.bleaf.comix.crawler.domain.marumaru;

import com.bleaf.comix.crawler.configuration.MarumaruConfig;
import com.bleaf.comix.crawler.configuration.UserAgent;
import com.bleaf.comix.crawler.domain.ComixCrawler;
import com.bleaf.comix.crawler.domain.dto.Comix;
import com.bleaf.comix.crawler.domain.utility.ComixUtil;
import com.bleaf.comix.crawler.domain.utility.StoreType;
import com.google.common.collect.Lists;
import com.google.common.net.UrlEscapers;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DailyCrawler implements ComixCrawler {
    @Autowired
    MarumaruConfig marumaruConfig;

    @Autowired
    ComixUtil comixUtil;

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

//

    public List<Comix> getDailyList(DateTime selectDate) throws IOException {
        Document rawData = comixUtil.getHtmlPageJsoup(
                marumaruConfig.getDailyUri(),
                StoreType.MARUMARU);

        // String pageSource에서 Jsoup doc으로 변경할 경우, base uri가 없으면
        // abs:href 에서 empty가 return 된다.

//        Document rawData = Jsoup.parse(pageSource, marumaruConfig.getBaseUri());

        // 일일 업데이트 게시판은 tabe tag를 이용하여 구성되어 있음.
        // 공지사항을 제외한 tr의 a 태그들을 얻어온다.
        Elements articles = rawData.select("tr:not(.tr_notice) a");

        List<Comix> comixes = Lists.newArrayList();
        Comix comix;
        for(Element article : articles) {

            // "abs: => url의 절대 값을 가지고 올 때 사용하는 키워드"
            // href="/c/26"일경우,
            // .attr("href") = /c/26
            // .attr("abs:href") = http://www.marumaru.in/c/26"
            String href = article.attr("abs:href");

            // a 태그 안에 포함된 div들
            Elements articleDiv = article.select("div");

            // 두 번째 div에서 제목을 얻어낸다.
            String title = comixUtil.checkTitle(articleDiv.get(1).ownText());
            String date = articleDiv
                    .get(1)
                    .select("small")
                    .text()
                    .split("\\|")[0]
                    .trim()
                    .split(" ")[0];

            log.info("title = {} : {}", title, date);
            log.info("comix url = {}", href);

            DateTime dateTime = DateTimeFormat
                    .forPattern(marumaruConfig.getDateFormat())
                    .parseDateTime(date);

            if(!selectDate.equals(dateTime)) {
                continue;
            }

            String volumnUri = getVolumnUri(href);
            String allSeriesUri = getAllSeriesUri(href);

            List<String> comixPage = getComixPage(volumnUri);

            comix = new Comix();
            comix.setTitle(title);
            comix.setUpdateDate(dateTime);
            comix.setShortComix(comixUtil.isShort(title));
            comix.setExts(comixUtil.getImageExtension(comixPage));

            comix.setComixUri(href);
            comix.setOneVolumnUri(volumnUri);
            comix.setAllSeriesUri(allSeriesUri);
            comix.setImageUris(comixPage);

            comix.setVolumn(comixUtil.getVolumn(title));

            log.debug("comix info = {}", comix);

            comixes.add(comix);
        }

        return comixes;
    }

    // 업데이트 된 화에 해당하는 uri를 돌려 준다
    public String getVolumnUri(String uri) throws IOException {
        Document rawData = comixUtil.getHtmlPageJsoup(uri, StoreType.MARUMARU);

//        Document rawData = Jsoup.parse(pageSource, marumaruConfig.getBaseUri());

        Elements contentATags = rawData.select("#vContent a"); // 공지사항을 제외한 tr의 a 태그들을 얻어온다.

        String viewPageUrl = contentATags.first()
                .attr("abs:href"); // 마찬가지로 절대주소 href를 얻어낸다

        log.info("comix info url = {}", viewPageUrl);


        return viewPageUrl;
    }

    // 업데이트 된 화에 해당하는 uri를 돌려 준다
    public String getAllSeriesUri(String uri) throws IOException {
        Document rawData = comixUtil.getHtmlPageJsoup(uri, StoreType.MARUMARU);

//        Document rawData = Jsoup.parse(pageSource, marumaruConfig.getBaseUri());

        Elements contentATags = rawData.select("#vContent a"); // 공지사항을 제외한 tr의 a 태그들을 얻어온다.

        String viewPageUrl = contentATags.next()
                .attr("abs:href"); // 마찬가지로 절대주소 href를 얻어낸다

        log.info("comix info url = {}", viewPageUrl);


        return viewPageUrl;
    }

    public List<String> getComixPage(String uri) throws IOException {
        Document rawData = comixUtil.getHtmlPageJsoup(uri, StoreType.MARUMARU);

//        Document rawData = Jsoup.parse(pageSource, );

        // lz-lazyload 클래스를 가진 img들
        Elements imgs = rawData.select("img[class=lz-lazyload]");

        List<String> imageUrls = new ArrayList<>();

        String imgUrl;
        for(Element img : imgs) {
            imgUrl = UrlEscapers.urlFragmentEscaper().escape(img.attr("abs:data-src"));
            log.debug("convert image url = {}", imgUrl);

            imageUrls.add(imgUrl);
        }

        return imageUrls;
    }
}

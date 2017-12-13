package com.bleaf.comix.crawler.domain.marumaru;

import com.bleaf.comix.crawler.configuration.ComixConfig;
import com.bleaf.comix.crawler.configuration.MarumaruConfig;
import com.bleaf.comix.crawler.domain.ComixCrawler;
import com.bleaf.comix.crawler.domain.dto.Comix;
import com.bleaf.comix.crawler.domain.utility.ComixUtil;
import com.bleaf.comix.crawler.domain.utility.HtmlParserUtil;
import com.bleaf.comix.crawler.domain.utility.StoreType;
import com.google.common.collect.Lists;
import com.google.common.net.UrlEscapers;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DateCrawler {
    @Autowired
    MarumaruConfig marumaruConfig;

    @Autowired
    ComixConfig comixConfig;

    @Autowired
    ComixUtil comixUtil;

    @Autowired
    HtmlParserUtil htmlParserUtil;

    public List<Comix> getComixList(DateTime today) {
        // String pageSource에서 Jsoup doc으로 변경할 경우, base uri가 없으면
        // abs:href 에서 empty가 return 된다.
//        Document rawData = Jsoup.parse(pageSource, marumaruConfig.getBaseUri());

        List<Comix> comixes = Lists.newArrayList();
        Path homePath = Paths.get(comixConfig.getBasePath(), today.toString("yyyyMMdd"));
        Path servicePath = Paths.get(comixConfig.getServicePath(), today.toString("yyyyMMdd"));

        Comix comix;

        String name, title, date, href, episodeUri, allEpisodeUri, episode;
        List<String> comixPage;

        DateTime dateTime = null;

        Document rawData;

        int pageNum = 1;
        String upateUri = marumaruConfig.getDailyUri();

        boolean stop = false;
        while(!stop) {
            rawData = htmlParserUtil.getHtmlPageJsoup(upateUri, StoreType.MARUMARU);

            // 일일 업데이트 게시판은 table tag를 이용하여 구성되어 있음.
            // 공지사항을 제외한 tr의 a 태그들을 얻어온다.
            Elements articles = rawData.select("tr:not(.tr_notice) a");

            for (Element article : articles) {

                // "abs: => url의 절대 값을 가지고 올 때 사용하는 키워드"
                // href="/c/26"일경우,
                // .attr("href") = /c/26
                // .attr("abs:href") = http://www.marumaru.in/c/26"
                href = article.attr("abs:href");

                // a 태그 안에 포함된 div들
                Elements articleDiv = article.select("div");

                // 두 번째 div에서 제목을 얻어낸다.
                title = comixUtil.checkTitle(articleDiv.get(1).ownText());
                date = articleDiv
                        .get(1)
                        .select("small")
                        .text()
                        .split("\\|")[0]
                        .trim()
                        .split(" ")[0];

                dateTime = DateTimeFormat
                        .forPattern(marumaruConfig.getDateFormat())
                        .parseDateTime(date);

                episode = comixUtil.getEpisode(title);

                if (!today.equals(dateTime)) {
                    continue;
                }

                try {
                    episodeUri = getEpisodeUri(href);
                    allEpisodeUri = getAllEpisodeUri(href);

                    comixPage = htmlParserUtil.getComixImageUri(episodeUri, StoreType.MARUMARU);

                    comix = new Comix();
                    comix.setName(comixUtil.getComixName(title));
                    comix.setTitle(title);
                    comix.setUpdateDate(dateTime);
                    comix.setShortComix(comixUtil.isShort(title));
                    comix.setExts(comixUtil.getImageExtension(comixPage));

                    comix.setComixUri(href);
                    comix.setEpisodeUri(episodeUri);
                    comix.setAllEpisodeUri(allEpisodeUri);
                    comix.setImageUris(comixPage);

                    comix.setDownloadPath(homePath.resolve(title));
                    comix.setServicePath(servicePath.resolve(title));

                    comix.setEpisode(comixUtil.getEpisode(title));

                    log.info(" ### comix info = {}", comix);

                    comixes.add(comix);
                } catch (IOException e) {
                    log.error(" ### fail crawling uri = {}", e.getMessage());
                }
            }

            if(!today.equals(dateTime)) {
                stop = true;
            }

            pageNum += 1;

            upateUri = this.getUpdateUri(pageNum, rawData);
        }

        return comixes;
    }

    // 업데이트 된 화에 해당하는 uri를 돌려 준다
    private String getEpisodeUri(String uri) throws IOException {
        Document rawData = htmlParserUtil.getHtmlPageJsoup(uri, StoreType.MARUMARU);

//        Document rawData = Jsoup.parse(pageSource, marumaruConfig.getBaseUri());

        Elements contentATags = rawData.select("#vContent a"); // 공지사항을 제외한 tr의 a 태그들을 얻어온다.

        String viewPageUrl = contentATags.first()
                .attr("abs:href"); // 마찬가지로 절대주소 href를 얻어낸다

        log.info("comix info url = {}", viewPageUrl);


        return viewPageUrl;
    }

    // 전체 화가 있는 page url을 돌려 준다.
    private String getAllEpisodeUri(String uri) throws IOException {
        Document rawData = htmlParserUtil.getHtmlPageJsoup(uri, StoreType.MARUMARU);

        Elements contentATags = rawData.select("#vContent a"); // 공지사항을 제외한 tr의 a 태그들을 얻어온다.

        String viewPageUrl = contentATags.get(1)
                .attr("abs:href"); // 마찬가지로 절대주소 href를 얻어낸다

        log.info("comix info url = {}", viewPageUrl);


        return viewPageUrl;
    }

    public String getUpdateUri(int pageNum, Document rawData) {
        // div class="pagebox[0]"의 a tag를 가지고 온다.
        Elements elements = rawData.select("div.pagebox01 a[class=notselected]");

        String strPageNum;
        for(Element element : elements) {
            strPageNum = element.ownText();

            if(strPageNum.equals("" + pageNum)) {
                return element.attr("abs:href");
            }
        }

        return null;
    }
}

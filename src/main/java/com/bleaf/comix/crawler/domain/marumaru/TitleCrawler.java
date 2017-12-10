package com.bleaf.comix.crawler.domain.marumaru;

import com.bleaf.comix.crawler.configuration.ComixConfig;
import com.bleaf.comix.crawler.configuration.MarumaruConfig;
import com.bleaf.comix.crawler.domain.ComixCrawler;
import com.bleaf.comix.crawler.domain.dto.Comix;
import com.bleaf.comix.crawler.domain.utility.ComixUtil;
import com.bleaf.comix.crawler.domain.utility.HtmlParserUtil;
import com.bleaf.comix.crawler.domain.utility.StoreType;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.net.UrlEscapers;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
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
import java.util.Map;

@Data
@Slf4j
@Service
public class TitleCrawler implements ComixCrawler {
    @Autowired
    MarumaruConfig marumaruConfig;

    @Autowired
    ComixConfig comixConfig;

    @Autowired
    ComixUtil comixUtil;

    @Autowired
    HtmlParserUtil htmlParserUtil;

    @Override
    public List<Comix> getComixList(String comixName) {
        // String pageSource에서 Jsoup doc으로 변경할 경우, base uri가 없으면
        // abs:href 에서 empty가 return 된다.
//        Document rawData = Jsoup.parse(pageSource, marumaruConfig.getBaseUri());

        List<Comix> comixes = Lists.newArrayList();
        Path homePath = Paths.get(comixConfig.getBasePath());
        Path servicePath = Paths.get(comixConfig.getServicePath());

        Comix comix;

        String name, title, date, href, episodeUri;
        Map<String, String> allEpisodeUriMap;
        List<String> comixPage;

        DateTime dateTime = null;

        Document rawData;

        int pageNum = 1;

        String searchUri = marumaruConfig.getTitleUri()
                + UrlEscapers.urlFormParameterEscaper().escape(comixName);

        log.info(" ### start download comix = {} : {}", comixName, searchUri);

        boolean stop = false;
        rawData = htmlParserUtil.getHtmlPageJsoup(searchUri, StoreType.MARUMARU);

        // 일일 업데이트 게시판은 table tag를 이용하여 구성되어 있음.
        // 공지사항을 제외한 tr의 a 태그들을 얻어온다.
        Elements articles = rawData.select("#s_post div.postbox a");

        for (Element article : articles) {
            // "abs: => url의 절대 값을 가지고 올 때 사용하는 키워드"
            // href="/c/26"일경우,
            // .attr("href") = /c/26
            // .attr("abs:href") = http://www.marumaru.in/c/26"
            // 제목에 해당하는 만화의 전체 화가 있는 page uri를 돌려준다.
            href = article.attr("abs:href");

            // a 태그 안에 포함된 div들
            Elements articleDiv = article.select("div.sbjbox b");

            // 두 번째 div에서 제목을 얻어낸다.
            title = comixUtil.checkTitle(articleDiv.get(0).ownText());

            if (!title.equalsIgnoreCase(comixName)) continue;

            allEpisodeUriMap = getAllEpisodeUri(href);

            if(allEpisodeUriMap.size() <= 0) {
                log.error(" 전체 uri을 가져오는데 실패하였습니다.");
                continue;
            }

            for(String episode : allEpisodeUriMap.keySet()) {
                episodeUri = allEpisodeUriMap.get(episode);

                log.debug(" ### episode {} image url parsing = {}", episodeUri);
                comixPage = htmlParserUtil.getComixImageUri(episodeUri, StoreType.MARUMARU);

                comix = new Comix();
                comix.setName(comixName);
                comix.setTitle(title);

                comix.setShortComix(allEpisodeUriMap.size() == 1);
                comix.setExts(comixUtil.getImageExtension(comixPage));

                comix.setComixUri(href);
                comix.setAllEpisodeUri(href);
                comix.setImageUris(comixPage);

                comix.setDownloadPath(homePath.resolve(title));
                comix.setServicePath(servicePath.resolve(title));

                comix.setEpisode(episode);

                log.info(" ### comix info = {}", comix);

                comixes.add(comix);
            }
        }

        return comixes;
    }

    @Override
    public List<Comix> getComixList(DateTime today) {
        return null;
    }

    private Map<String, String> getAllEpisodeUri(String uri) {
        Preconditions.checkNotNull(uri, "uri is null", uri);

        Map<String, String> episodeUriMap = Maps.newLinkedHashMap();

        Document rawData = htmlParserUtil.getHtmlPageJsoup(uri, StoreType.MARUMARU);
        Elements contentATags = rawData.select("#vContent a");

        String href, title, episode;
        for(Element cont : contentATags) {
            href = cont.attr("abs:href");
            title = comixUtil.checkTitle(cont.ownText());

            episode = comixUtil.getEpisode(title);

            episodeUriMap.put(episode, href);
        }

        return episodeUriMap;
    }

}

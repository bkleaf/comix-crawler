package com.bleaf.comix.crawler.domain.marumaru;

import com.bleaf.comix.crawler.configuration.ComixConfig;
import com.bleaf.comix.crawler.configuration.MarumaruConfig;
import com.bleaf.comix.crawler.domain.ComixCrawler;
import com.bleaf.comix.crawler.domain.dto.Comix;
import com.bleaf.comix.crawler.domain.utility.ComixUtil;
import com.bleaf.comix.crawler.domain.utility.HtmlParserUtil;
import com.bleaf.comix.crawler.domain.utility.StoreType;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@Service
public class TitleCrawler {
    @Autowired
    MarumaruConfig marumaruConfig;

    @Autowired
    ComixConfig comixConfig;

    @Autowired
    ComixUtil comixUtil;

    @Autowired
    HtmlParserUtil htmlParserUtil;

    public List<Comix> getComixList(String comixName, String range) {
        List<Comix> comixes = Lists.newArrayList();

        // base path / {comixName} / {comixName} + 화
        // 구조를 가져야 해서 home 및 service는 기본 paht에 코믹스 네임 밑으로 생성 시킨다
        Path homePath = Paths.get(comixConfig.getBasePath(), comixUtil.checkTitle(comixName));
        Path servicePath = Paths.get(comixConfig.getServicePath(), comixUtil.checkTitle(comixName));

        Comix comix;

        String originalTitle, href, episodeUri;
        boolean shortComix;
        Map<String, String> allEpisodeUriMap;
        List<String> comixPage;

        Document rawData;

        String searchUri = marumaruConfig.getTitleUri()
                + UrlEscapers.urlFormParameterEscaper().escape(comixName);

        log.info(" ### start download comix = {} : {}", comixName, searchUri);

        rawData = htmlParserUtil.getHtmlPageJsoup(searchUri, StoreType.MARUMARU);

        // 일일 업데이트 게시판은 table tag를 이용하여 구성되어 있음.
        // 공지사항을 제외한 tr의 a 태그들을 얻어온다.
        Elements articles = rawData.select("#s_post div.postbox a");

        String episode = null;
        List<String> episodeList = comixUtil.getEpisodeRange(range);
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
            originalTitle = articleDiv.get(0).ownText();

            if (!originalTitle.equalsIgnoreCase(comixName)) continue;

            allEpisodeUriMap = getAllEpisodeUri(href);

            if(allEpisodeUriMap.size() <= 0) {
                log.error(" 전체 uri을 가져오는데 실패하였습니다.");
                continue;
            }

            for(String comixTitle : allEpisodeUriMap.keySet()) {
                episodeUri = allEpisodeUriMap.get(comixTitle);

                shortComix = comixUtil.isShort(comixTitle);

                if(!shortComix) {
                    episode = comixUtil.getEpisode(comixTitle);

                    if(episodeList != null && !episodeList.contains(episode)) {
                        continue;
                    }
                }

                log.debug(" ### episode {} image url parsing = {}", episodeUri);
                comixPage = htmlParserUtil.getComixImageUri(episodeUri, StoreType.MARUMARU);

                comix = new Comix();
                comix.setName(comixName);
                comix.setTitle(comixTitle);

                comix.setShortComix(allEpisodeUriMap.size() == 1);
                comix.setExts(comixUtil.getImageExtension(comixPage));

                comix.setComixUri(href);
                comix.setAllEpisodeUri(href);
                comix.setImageUris(comixPage);

                comix.setDownloadPath(homePath.resolve(comixTitle));
                comix.setServicePath(servicePath.resolve(comixTitle));

                if(shortComix) {
                    comix.setEpisode(episode);
                }

                log.info(" ### comix info = {}", comix);

                comixes.add(comix);
            }
        }

        return comixes;
    }

    private Map<String, String> getAllEpisodeUri(String uri) {
        Preconditions.checkNotNull(uri, "uri is null", uri);

        Map<String, String> episodeUriMap = Maps.newLinkedHashMap();

        Document rawData = htmlParserUtil.getHtmlPageJsoup(uri, StoreType.MARUMARU);
        Elements contentATags = rawData.select("#vContent a");

        String href, title;
        for(Element cont : contentATags) {
            href = cont.attr("abs:href");

            if(!comixUtil.isImageUrl(href)) {
                continue;
            }

            title = comixUtil.checkTitle(cont.ownText());

            if(Strings.isNullOrEmpty(title)) {
                title = comixUtil.checkTitle(cont.getElementsByTag("font").text());
            }

            episodeUriMap.put(title, href);
        }

        return episodeUriMap;
    }

}

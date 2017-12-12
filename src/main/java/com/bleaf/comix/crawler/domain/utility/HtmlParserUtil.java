package com.bleaf.comix.crawler.domain.utility;

import com.bleaf.comix.crawler.configuration.MarumaruConfig;
import com.bleaf.comix.crawler.configuration.UserAgent;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.google.common.net.UrlEscapers;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class HtmlParserUtil {
    @Autowired
    ComixUtil comixUtil;

    @Autowired
    MarumaruConfig marumaruConfig;

    public List<String> getComixImageUri(String uri, StoreType storeType) {
        try {
            String host = new URL(uri).getHost();
            if(CharMatcher.anyOf("wasabisyrub").matchesAllOf(uri)) {
                return this.getImageUriFromWasabi(uri, storeType);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<String> getImageUriFromWasabi(String uri, StoreType storeType) throws IOException {
//        Document rawData = this.getHtmlPageJsoup(uri, storeType);

        Document rawData = null;
        try {
            Connection.Response response = Jsoup.connect("http://wasabisyrup.com/archives/qfyfvtOPgKQ?type=pass")
                    .userAgent(UserAgent.getUserAgent())
                    .header("charset", "utf-8")
                    .header("Accept-Encoding", "gzip") //20171126 gzip 추가
                    .data("Cookie", "__cfduid=d2e577e6d8f5ffc55a86d95b3dbf95d7d1513035663; PHPSESSID=94b70d1b9c78f4476a0c8dd322963fda; _ga=GA1.2.1158025461.1513035662; _gid=GA1.2.1794056051.1513035662; cf_clearance=59c34d2be3544ac3421164bef6ca7e70fca7cacd-1513035848-3600")
                    .data("pass", "qndxkr")
                    .data("Referer", "http://wasabisyrup.com/archives/qfyfvtOPgKQ?type=pass")
                    .timeout(5000)
                    .method(Connection.Method.POST)
                    .execute();

            rawData = response.parse();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
}

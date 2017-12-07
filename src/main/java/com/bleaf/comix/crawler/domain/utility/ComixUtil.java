package com.bleaf.comix.crawler.domain.utility;

import com.bleaf.comix.crawler.configuration.MarumaruConfig;
import com.bleaf.comix.crawler.configuration.UserAgent;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;

@Slf4j
@Component
public class ComixUtil {
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

    public String getListHtmlPage(String listUrl, StoreType storeType) {
        String pageSource = getHtmlPageJsoup(listUrl, storeType);
        if(pageSource == null) {
            pageSource = getHtmlPageHtmlUnit(listUrl, storeType);
        }

        return pageSource;
    }

    private String getHtmlPageJsoup(String listUrl, StoreType storeType){
        String pageSource = null;

        String password = "";
        if(storeType == StoreType.MARUMARU) {
            password = marumaruConfig.getPassword();
        }

        try {
            Document preDoc = Jsoup.connect(listUrl)
                    .userAgent(UserAgent.getUserAgent())
                    .header("charset", "utf-8")
                    .header("Accept-Encoding", "gzip") //20171126 gzip 추가
                    .data("pass", password)
                    .timeout(5000)
                    .get();

            pageSource = preDoc.toString();
        }
        catch (Exception e) {
            log.error("error html parsing = {} : {}", listUrl, e.getMessage());
        }

        return pageSource;
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

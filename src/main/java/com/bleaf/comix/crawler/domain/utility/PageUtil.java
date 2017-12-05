package com.bleaf.comix.crawler.domain.utility;

import com.bleaf.comix.crawler.configuration.ComixUrlConfig;
import com.bleaf.comix.crawler.configuration.UserAgent;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;

@Slf4j
@Component
public class PageUtil {
    @Autowired
    ComixUrlConfig comixUrlConfig;

    public String getListHtmlPage(String listUrl) {
        String pageSource = getHtmlPageJsoup(listUrl);
        if(pageSource == null) {
            pageSource = getHtmlPageHtmlUnit(listUrl);
        }

        return pageSource;
    }

    private String getHtmlPageJsoup(String listUrl){
        String pageSource = null;

        try {
            Connection.Response response = Jsoup.connect(listUrl)
                    .userAgent(UserAgent.getUserAgent())
                    .header("charset", "utf-8")
                    .header("Accept-Encoding", "gzip") //20171126 gzip 추가
                    .data("pass", comixUrlConfig.getMarumaruPassword())
                    .followRedirects(true)
                    .execute();
            Document preDoc = response.parse();
        }
        catch (Exception e) {
            log.error("error html parsing = {} : {}", listUrl, e.getMessage());
        }

        return pageSource;
    }

    private String getHtmlPageHtmlUnit(String listUrl){
        String pageSource = null;

        WebClient webClient = new WebClient();
        webClient.getOptions().setRedirectEnabled(true);

        try{
            WebRequest req = new WebRequest(new URL(listUrl));
            req.setAdditionalHeader("User-Agent", UserAgent.getUserAgent());
            req.setAdditionalHeader("Accept-Encoding", "gzip");
            req.setHttpMethod(HttpMethod.POST);
            req.getRequestParameters().add(new NameValuePair("pass", comixUrlConfig.getMarumaruPassword()));
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

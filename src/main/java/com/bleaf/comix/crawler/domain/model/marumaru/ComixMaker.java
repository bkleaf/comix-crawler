package com.bleaf.comix.crawler.domain.model.marumaru;

import com.bleaf.comix.crawler.domain.dto.Comix;
import com.bleaf.comix.crawler.domain.utility.HtmlParserUtil;
import com.bleaf.comix.crawler.domain.utility.StoreType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Data
@Component
public class ComixMaker {
    @Autowired
    HtmlParserUtil htmlParserUtil;

    public List<Comix> make(String makeUri) {
        Document document = htmlParserUtil.getHtmlPageJsoup(makeUri, StoreType.MARUMARU);

        String subject = document.select("div.subject").tagName("h1").text();
        return null;
    }
}

package com.bleaf.comix.crawler.domain.crawler.marumaru;


import com.bleaf.comix.crawler.configuration.MarumaruConfig;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class MarumaruService {
    @Autowired
    MarumaruConfig marumaruConfig;

    public Map<String, String> setPhpSessionId(String id) {
        Map<String, String> cookies = Maps.newHashMap();
        cookies.put("PHPSESSID", id);

        marumaruConfig.setCookies(cookies);

        log.info("set marumaru session id = {}", marumaruConfig.getCookies());

        return marumaruConfig.getCookies();
    }
}

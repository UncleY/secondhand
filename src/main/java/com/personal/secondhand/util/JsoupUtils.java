package com.personal.secondhand.util;


import com.personal.secondhand.constants.CommonConstants;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import static com.personal.secondhand.constants.CommonConstants.ENCODING;
import static com.personal.secondhand.constants.CommonConstants.TIME_OUT;

public class JsoupUtils {

    /**
     * 获取jsoup 的connection
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static Connection connect(String url) throws Exception {
        return Jsoup.connect(url)
                .userAgent(CommonConstants.getRandomUserAgent())
                .postDataCharset(ENCODING)
                .timeout(TIME_OUT);
    }


    private JsoupUtils() {
    }
}

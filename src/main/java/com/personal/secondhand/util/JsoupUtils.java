package com.personal.secondhand.util;


import com.personal.secondhand.constants.CommonConstants;
import org.apache.commons.lang3.StringUtils;
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
        if (StringUtils.startsWith(url, "//")) {
            url = "https:" + url;
        }
        return Jsoup.connect(url)
                // 代理
                .userAgent(CommonConstants.getRandomUserAgent())
                // 编码格式
                .postDataCharset(ENCODING)
                // 超时时间
                .timeout(TIME_OUT)
                // 不设置返回大小限制
                .maxBodySize(0);
    }


    private JsoupUtils() {
    }
}

package com.personal.secondhand.util;


import com.personal.secondhand.constants.CommonConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.personal.secondhand.constants.CommonConstants.ENCODING;
import static com.personal.secondhand.constants.CommonConstants.TIME_OUT;

@Slf4j
public class JsoupUtils {

    private static List<String> PROXY_IP = new ArrayList<>(0);

    static {
        PROXY_IP.addAll(getProxyIp());
    }

    /**
     * 根据静态html解析内容
     *
     * @param html
     * @return
     * @throws Exception
     */
    public static Document parse(String html) {
        return Jsoup.parse(html);
    }

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
        List<String> ipList = getProxyIp();
        String ipContent = ipList.get(CommonConstants.localRandom.nextInt(ipList.size()));
        String ip = "";
        String port = "";
        if (StringUtils.isNotBlank(ipContent)) {
            String[] proxy = ipContent.split(":");
            ip = proxy[0];
            port = proxy[1];
        }
        System.out.println(ipContent);
        return Jsoup.connect(url)
                .header("Host", "webim.58.com")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("DNT", "1")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .header("TE", "Trailers")
                .proxy(ip, Integer.parseInt(port))
                // 代理
                .userAgent(CommonConstants.getRandomUserAgent())
                // 编码格式
                .postDataCharset(ENCODING)
                // 超时时间
                .timeout(TIME_OUT)
                // 不设置返回大小限制
                .maxBodySize(0);
    }


    /**
     * 随机获取ip代理
     *
     * @return
     */
    private static List<String> getProxyIp() {
        List<String> ipList = new ArrayList<>(0);

        try {
            Thread.sleep(1500);
            String s = Jsoup.connect("http://api.xedl.321194.com/getip?num=10&type=1&port=11&pack=4055&ts=0&cs=1&lb=1")
                    .ignoreContentType(true)
                    .timeout(5000)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("DNT", "1")
                    .header("Connection", "keep-alive")
                    .userAgent(CommonConstants.getRandomUserAgent())
                    .execute()
                    .body();
            String[] strings = s.split("\r\n");
            ipList.addAll(Arrays.asList(strings));
        } catch (Exception e) {
            log.error("获取随机代理ip异常", e);
        }
        return ipList;
    }


    private JsoupUtils() {
    }
}

package com.personal.secondhand.constants;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 公共定义常量
 *
 * @author yangrui
 * @date 2019-8-1 23:21:43
 */
@Slf4j
public class CommonConstants {
    /**
     * 随机种子
     */
    public static final ThreadLocalRandom localRandom = ThreadLocalRandom.current();
    /**
     * 模拟agent的集合
     */
    private static List<String> USER_AGENTS_LIST = new ImmutableList.Builder<String>()
            .add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; AcooBrowser; .NET CLR 1.1.4322; .NET CLR 2.0.50727)")
            .add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Acoo Browser; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506)")
            .add("Mozilla/4.0 (compatible; MSIE 7.0; AOL 9.5; AOLBuild 4337.35; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)")
            .add("Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)")
            .add("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET CLR 2.0.50727; Media Center PC 6.0)")
            .add("Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET CLR 1.0.3705; .NET CLR 1.1.4322)")
            .add("Mozilla/4.0 (compatible; MSIE 7.0b; Windows NT 5.2; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.2; .NET CLR 3.0.04506.30)")
            .add("Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN) AppleWebKit/523.15 (KHTML, like Gecko, Safari/419.3) Arora/0.3 (Change: 287 c9dfb30)")
            .add("Mozilla/5.0 (X11; U; Linux; en-US) AppleWebKit/527+ (KHTML, like Gecko, Safari/419.3) Arora/0.6")
            .add("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.2pre) Gecko/20070215 K-Ninja/2.1.1")
            .add("Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9) Gecko/20080705 Firefox/3.0 Kapiko/3.0")
            .add("Mozilla/5.0 (X11; Linux i686; U;) Gecko/20070322 Kazehakase/0.4.5")
            .add("Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.8) Gecko Fedora/1.9.0.8-1.fc10 Kazehakase/0.5.6")
            .add("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11")
            .add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.20 (KHTML, like Gecko) Chrome/19.0.1036.7 Safari/535.20")
            .add("Opera/9.80 (Macintosh; Intel Mac OS X 10.6.8; U; fr) Presto/2.9.168 Version/11.52")
            .add("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.11 TaoBrowser/2.0 Safari/536.11")
            .add("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.71 Safari/537.1 LBBROWSER")
            .add("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; LBBROWSER)")
            .add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; QQDownload 732; .NET4.0C; .NET4.0E; LBBROWSER)")
            .add("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.84 Safari/535.11 LBBROWSER")
            .add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E)")
            .add("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; QQBrowser/7.0.3698.400)")
            .add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; QQDownload 732; .NET4.0C; .NET4.0E)")
            .add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; SV1; QQDownload 732; .NET4.0C; .NET4.0E; 360SE)")
            .add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; QQDownload 732; .NET4.0C; .NET4.0E)")
            .add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E)")
            .add("Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.89 Safari/537.1")
            .add("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.89 Safari/537.1")
            .add("Mozilla/5.0 (iPad; U; CPU OS 4_2_1 like Mac OS X; zh-cn) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148 Safari/6533.18.5")
            .add("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:2.0b13pre) Gecko/20110307 Firefox/4.0b13pre")
            .add("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:16.0) Gecko/20100101 Firefox/16.0")
            .add("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11")
            .add("Mozilla/5.0 (X11; U; Linux x86_64; zh-CN; rv:1.9.2.10) Gecko/20100922 Ubuntu/10.10 (maverick) Firefox/3.6.10")
            .build();
    /**
     * 默认编码格式
     */
    public static final String ENCODING = "UTF-8";
    /**
     * timeout 默认30秒
     */
    public static final int TIME_OUT = 30000;

    /**
     * ip proxy
     */
    public static List<String> PROXY_IP = new ArrayList<>(0);

    static {
        try {
            List<String> proxyList = FileUtils.readLines(new File(getProxyFile()), ENCODING);
            if (CollectionUtils.isEmpty(proxyList)) {
                createProxyIpFile();
            }
            proxyList = FileUtils.readLines(new File(getProxyFile()), ENCODING);
            PROXY_IP.addAll(proxyList);
        } catch (Exception e) {
            log.error("【加载代理ip文件异常】", e);
        }
    }

    public static Map<String, String> HEADER = new ImmutableMap.Builder<String, String>()
            .put("Host", "webim.58.com")
            .put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
            .put("Accept-Encoding", "gzip, deflate, br")
            .put("DNT", "1")
            .put("Connection", "keep-alive")
            .put("Upgrade-Insecure-Requests", "1")
            .put("TE", "Trailers")
            .build();

    private static Map<String, String> FONT_SECRET = new ImmutableMap.Builder<String, String>()
            .put("閏", "0x958f")
            .put("鸺", "0x9e3a")
            .put("麣", "0x9ea3")
            .put("餼", "0x993c")
            .put("鑶", "0x9476")
            .put("龤", "0x9fa4")
            .put("齤", "0x9f64")
            .put("龥", "0x9fa5")
            .put("龒", "0x9f92")
            .put("驋", "0x9a4b")
            .build();


    /**
     * 获取随机user agent
     *
     * @return
     */
    public static String getRandomUserAgent() {
        return USER_AGENTS_LIST.get(localRandom.nextInt(USER_AGENTS_LIST.size()));
    }

    private CommonConstants() {
    }

    private static String getProxyFile() {
        return CommonConstants.class.getClassLoader().getResource("proxy.txt").getFile();
    }

    /**
     * 随机获取ip代理
     *
     * @return
     */
    public static void createProxyIpFile() {
        List<String> ipList = new ArrayList<>(0);
        try {
            String s = getProxyIp("http://api.xedl.321194.com/getip?num=1&type=1&port=11&pack=4055&ts=0&cs=1&lb=1");
            if (StringUtils.indexOf(s, "您的套餐今日已到达上限") != -1) {
                log.error("api.xedl.321194.com获取50个随机代理今日已上限->{}", s);
                // 这个代理是每天20个
                s = getProxyIp("http://http.tiqu.qingjuhe.cn/getip?num=1&type=1&pack=35869&port=11&lb=1&pb=4&regions=110000,130000,140000,150000,210000,310000,320000,330000,340000,350000,360000,370000,410000,430000,440000,500000,510000,530000,610000,620000,640000");
                if (StringUtils.indexOf(s, "上限") != -1) {
                    log.error("http.tiqu.qingjuhe.cn获取随机代理今日20个已上限了->{}", s);
                    log.info("下血本花钱包周了");
//                    s = getProxyIp("http://http.tiqu.qingjuhe.cn/getip?num=10&type=1&pack=35871&port=11&lb=1&pb=4&regions=110000,130000,140000,150000,210000,310000,320000,330000,340000,350000,360000,370000,410000,430000,440000,500000,510000,530000,610000,620000,640000");
                    s = getProxyIp("http://http.tiqu.qingjuhe.cn/getip?num=5&type=1&pack=35872&port=11&lb=1&pb=4&regions=110000,130000,140000,150000,210000,310000,320000,330000,340000,350000,360000,370000,410000,430000,440000,500000,510000,530000,610000,620000,640000");
                    if (StringUtils.indexOf(s, "上限") != -1) {
                        log.error("收费代理到期：老哥，充钱吧！->{}", s);
                        return;
                    }
                }
            }
            log.info(s);
            String[] strings = s.split("\r\n");
            ipList.addAll(Arrays.asList(strings));
            if (CollectionUtils.isNotEmpty(ipList)) {
                File file = new File(getProxyFile());
                FileUtils.touch(file);
                FileUtils.writeLines(file, ipList, true);
            }
        } catch (Exception e) {
            log.error("获取随机代理ip异常", e);
        }
    }


    private static String getProxyIp(String proxyApiUrl) throws Exception {
        return Jsoup.connect(proxyApiUrl)
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
    }
}

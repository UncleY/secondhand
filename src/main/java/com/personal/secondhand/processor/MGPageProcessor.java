package com.personal.secondhand.processor;

import com.personal.secondhand.constants.CommonConstants;
import com.personal.secondhand.download.WebDriverDownloader;
import com.personal.secondhand.pipeline.FileInfoPipeline;
import com.personal.secondhand.pipeline.FilePagePipeline;
import com.personal.secondhand.util.JsoupUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * 芒果517
 * asp网站 页面访问较慢 并且有动态js跳转 需要模拟浏览器访问
 * WebDriverDownloader进行访问
 */
@Slf4j
public class MGPageProcessor implements PageProcessor {

    @Override
    public void process(Page page) {
        String url = page.getUrl().get();
        String html = page.getHtml().get();
        if (StringUtils.indexOf(html, "对不起，您来晚了，该房源已经下架了！") != -1) {
            log.info("详情房源已下架 url->{}", url);
            return;
        }

        if (StringUtils.indexOf(html, "正在加载......") != -1
                || StringUtils.indexOf(html, "应用程序中的服务器错误") != -1
                || StringUtils.indexOf(html, "upstream_status:504") != -1
                ) {
            // 未加载完全 休眠一会在扔回任务里
            try {
                Thread.sleep(3000L);
            } catch (Exception e) {
                log.error("休眠异常", e);
            }
            page.addTargetRequest(url);

        } else if (StringUtils.startsWith(url, "https://www.517.cn/ershoufang/osj1/area/pg")) {
            log.info("列表页面{}", url);
            Document document = JsoupUtils.parse(html);

            Elements elements = document.select("div[class=ListBox-I clearfix]");
            if (CollectionUtils.isEmpty(elements)) {
                log.info("页面没有列表信息-" + url);
                // 页面没加载完全
                try {
                    Thread.sleep(3000L);
                } catch (Exception e) {
                    log.error("休眠异常", e);
                }
                // 页面没加载完全
                page.addTargetRequest(url);
            } else {
                for (Element ele : elements) {
                    String nextUrl = ele.select("div[class=LB-f-img] a").attr("href");
                    // 放入详情url
                    page.addTargetRequest(nextUrl);
                }
                // 存储本地列表页
                page.putField("pageHtml", html);
            }


        } else if (page.getUrl().regex("https://www\\.517\\.cn/ershoufang/[0-9]{7}/*").match()) {
            log.info("详情页{}", url);
            page.putField("infoHtml", html);
        } else {
            log.error("未知页面{}", url);
        }
    }

    @Override
    public Site getSite() {
        Site site = Site.me()
                .setCharset(CommonConstants.ENCODING)
                .setUserAgent(CommonConstants.getRandomUserAgent())
                .addHeader("Host", "www.517.cn")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Accept-Encoding", "gzip, deflate, br")
                .addHeader("Referer", "https://www.517.cn/ershoufang/")
                .addHeader("Connection", "keep-alive")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("Cache-Control", "max-age=0")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Cookie", "UM_distinctid=16c6bfe67207d1-009a7b1084046a8-4c312272-1fa400-16c6bfe6721ee; CNZZDATA1272232378=1280959118-1565178473-https%253A%252F%252Fwww.baidu.com%252F%7C1565188315; Hm_lvt_1f8e83330039496a701d7ae44836ed10=1565179931; Hm_lpvt_1f8e83330039496a701d7ae44836ed10=1565190075; Hm_cv_1f8e83330039496a701d7ae44836ed10=1*userType*visitor; BPC=c56cc9984d87ac78d87bfcaa545f8b1d; website_qiehuan_cityid=1; ASP.NET_SessionId=nzal3n1extpi4yq3srsnn41h; historySellHouse=3213612|https://img76.517cdn.com/up2019/mg/2019/8/5/17/house/viewimg/fid3213612_uid32839_20190805175338343ade.jpg.2911.524x393.0.jpg|è¶\u008Aç§\u0080æ\u0098\u009Fæ±\u0087äº\u0091é\u0094¦ä¸\u0080æ\u009C\u009F ä¸¤å®¤ä¸¤å\u008E\u0085ä¸¤å\u008D« æ²³æ\u0099¯æ\u0088¿ |245.00|2|2|2|123.19|å\u008D\u0097||19888.00ã\u0080\u00852657111|https://img76.517cdn.com/up2019/mg/2019/7/2/12/house/viewimg/fid2657111_uid35581_20190702120104325865.jpg.2911.524x393.0.jpg|å\u008A³å\u008A¨å\u0085¬å\u009B\u00ADæ\u0097\u0081 ç²¾è£\u0085ä¸¤å®¤ å\u009C°ç\u0083\u00ADæ\u0088¿ èµ é\u0080\u0081å\u0085¨å±\u008Bå®¶å\u0085·å®¶ç\u0094µ é\u009A\u008Fæ\u0097¶ç\u009C\u008B|53.00|2|1|1|64.24|ä¸\u009Cè¥¿||8250.00ã\u0080\u00852889097|https://g.517cdn.com/www517cn/2016v1/images/noimg_big.png|é\u0095¿æ±\u009Få\u008D\u0097å°\u008Få\u008Cº å\u008D\u0097å\u0090\u0091 ä¸\u008Dæ\u008A\u008Aå±±ä¸\u008Dä¸´è¡\u0097|30.00|1|1|1|38.70|å\u008D\u0097||7752.00ã\u0080\u00853003919%7chttps%3a%2f%2fimg76.517cdn.com%2fup2019%2fmg%2f2019%2f7%2f31%2f14%2fhouse%2fviewimg%2ffid3003919_uid51209_201907311440179b5810.jpg.2911.524x393.0.jpg%7c%e5%85%b4%e9%be%99%e8%8b%912%e6%a5%bc%e4%b8%8d%e6%8a%8a%e5%b1%b1%e4%b8%8d%e4%b8%b4%e8%a1%97%e5%8d%97%e5%8c%97%e6%a0%87%e6%88%b7%ef%bc%81%7c126.00%7c2%7c1%7c1%7c112.53%7c%e5%8d%97%e5%8c%97%7c%7c11197.00%e3%80%85; web_views_count_xq=[{\"Id\":541,\"Sc\":1,\"Rc\":0,\"Xc\":0,\"Count\":2,\"Lt\":\"\\/Date(1565190070331)\\/\"},{\"Id\":540,\"Sc\":1,\"Rc\":0,\"Xc\":0,\"Count\":2,\"Lt\":\"\\/Date(1565189857052)\\/\"},{\"Id\":10034,\"Sc\":2,\"Rc\":0,\"Xc\":0,\"Count\":4,\"Lt\":\"\\/Date(1565181230324)\\/\"},{\"Id\":10683,\"Sc\":1,\"Rc\":0,\"Xc\":0,\"Count\":2,\"Lt\":\"\\/Date(1565180502739)\\/\"}]; ExtensionUserID=51209; VerifyCode=av4b; website_cityid=1; website_cityname=%e6%b2%88%e9%98%b3")
                .setRetryTimes(3)
                .setTimeOut(CommonConstants.TIME_OUT)
                .setSleepTime(CommonConstants.SLEEP_TIME)
                .setCycleRetryTimes(3);
        return site;
    }

//    public static void main(String[] args) throws Exception {
//        File file = new File("C:\\Users\\guosen\\Desktop\\517html.html");
//        String html = FileUtils.readFileToString(file, "utf-8");
//        Document document = JsoupUtils.parse(html);
//        Elements elements = document.select("div[class=ListBox-I clearfix]");
//        for (Element ele : elements) {
//            String url = ele.select("div[class=LB-f-img] a").attr("href");
//            String linkman = ele.select("div[class=price] p[class=P-gx] a").attr("href");
//            String pid = linkman.replaceAll("/s", "").replaceAll("/", "");
//            System.out.println(url);
//            System.out.println(linkman);
//            System.out.println(pid);
//            System.out.println("#############");
//        }
//
//
//    }

    public static void main(String[] args) throws Exception {
        // 1~100 除了6个推荐位都是今日更新
//        String url = "https://www.517.cn/ershoufang/osj1/area/pg数字/?ckattempt=1";
        String today = new DateTime().toString("yyyyMMdd");
        Spider spider = Spider.create(new MGPageProcessor()).setUUID(today);

        int startPage = 1;
        int endPage = 100;
        String downloadPath = CommonConstants.DOWNLOAD_FILE_PATH + "/517html/pc/";
        for (int i = startPage; i <= endPage; i++) {
            spider.addUrl("https://www.517.cn/ershoufang/osj1/area/pg" + i + "/?ckattempt=1");
        }
        spider.setDownloader(new WebDriverDownloader());
        spider.addPipeline(new FilePagePipeline(downloadPath));
        spider.addPipeline(new FileInfoPipeline(downloadPath));
        spider.thread((Runtime.getRuntime().availableProcessors() - 1) << 1);
        spider.run();
    }

}

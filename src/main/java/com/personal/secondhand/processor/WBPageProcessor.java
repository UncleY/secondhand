package com.personal.secondhand.processor;

import com.personal.secondhand.Application;
import com.personal.secondhand.constants.CommonConstants;
import com.personal.secondhand.pipeline.WBPipeline;
import com.personal.secondhand.vo.HouseInfo58;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.List;


public class WBPageProcessor implements PageProcessor {

    private static String REGEX_PAGE_URL = "https://sy\\.58\\.com/ershoufang/\\d+\\x\\.shtml*";

    private Site site = Site.me()
            .setUserAgent(CommonConstants.getRandomUserAgent())
            .setCharset(CommonConstants.ENCODING)
            .addHeader("Host", "sy.58.com")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
            .addHeader("Accept-Encoding", "gzip, deflate, br")
            .addHeader("DNT", "1")
            .addHeader("Cache-Control", "max-age=0")
            .addHeader("Cookie", "userid360_xml=84FDE92D1BAF969AF519B90812CFCF13; time_create=1567262816215; f=n; commontopbar_new_city_info=188%7C%E6%B2%88%E9%98%B3%7Csy; commontopbar_ipcity=sz%7C%E6%B7%B1%E5%9C%B3%7C0; id58=c5/nn10ylddQ114tnqyDAg==; 58home=sy; city=sy; 58tj_uuid=8e18e6a5-4c78-4b06-8864-9452f3cd3c91; new_uv=15; als=0; xxzl_deviceid=cWfZMCxyit2BPkbIU%2Bd8a8FdTbw2l9sEChCtC%2F7SrH85HAiC9SoYJ3chn9DmdGbN; wmda_uuid=076052445fe0f88b1f1c416e1906d6ff; wmda_new_uuid=1; wmda_visited_projects=%3B6333604277682; JSESSIONID=86D6CA386D6ACA2553666590E007AA15; wmda_session_id_6333604277682=1565103322280-a2aef2fe-1598-081a; new_session=0; utm_source=; spm=; init_refer=; f=n; commontopbar_new_city_info=188%7C%E6%B2%88%E9%98%B3%7Csy; commontopbar_ipcity=sz%7C%E6%B7%B1%E5%9C%B3%7C0; ppStore_fingerprint=A8406BD5518095CDDD4B89FB58ED8926DB3B2A1E581BE2E1%EF%BC%BF1565103325066; xzfzqtoken=WxHgr%2BG%2FATERuWQXssaV209DmpYkUYMmE34pZ7C5Yk8FSvAWKdeDHq7Zr3h9cRVXin35brBb%2F%2FeSODvMgkQULA%3D%3D")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("Referer", "https://sy.58.com/")
            .addHeader("Connection", "keep-alive")
            .setRetryTimes(3)
            .setTimeOut(CommonConstants.TIME_OUT)
            .setSleepTime(3000);

    @Override
    public void process(Page page) {
        String url = page.getRequest().getUrl();
        System.out.println(url);
//        System.out.println(page.getHtml().get());
        if (url.startsWith("https://sy.58.com/ershoufang/3")) {
            System.out.println("详情页");
            HouseInfo58 model = Application.parse58Info(page.getHtml().get());

            page.putField("model", model);
        } else if (StringUtils.startsWith(url, "https://sy.58.com/ershoufang/0/pn")) {
            System.out.println("列表页");

            Document document = page.getHtml().getDocument();
            List<String> urlList = new ArrayList<>(0);
            // 128条子数据
            Elements list_li = document.select("[class='house-list-wrap'] li");
            for (Element every : list_li) {
                Elements pic = every.select("[class=pic]").select("a");
                String href = pic.attr("href");
                System.out.println(href);
                urlList.add(href);
            }
            page.addTargetRequests(urlList);


        } else {
            System.out.println("未匹配的url->" + url);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new WBPageProcessor())
                //从url开始抓
                .addUrl("https://sy.58.com/ershoufang/0/pn1")
                .addPipeline(new WBPipeline())
                //开启5个线程抓取
                .thread(4)
                //启动爬虫
                .run();

    }
}

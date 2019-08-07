package com.personal.secondhand.processor;

import com.personal.secondhand.Application;
import com.personal.secondhand.constants.CommonConstants;
import com.personal.secondhand.vo.HouseInfo58;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * 五八页面抽取进程
 */
@Slf4j
public class WBPageProcessor implements PageProcessor {

    private static String REGEX_PAGE_URL = "https://sy\\.58\\.com/ershoufang/[0-9]{14}x\\.shtml*";

    private Site site = Site.me()
            .setUserAgent(CommonConstants.getRandomUserAgent())
            .setCharset(CommonConstants.ENCODING)
            .addHeader("Host", "sy.58.com")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
            .addHeader("Accept-Encoding", "gzip, deflate, br")
            .addHeader("DNT", "1")
            .addHeader("Cache-Control", "max-age=0")
            .addHeader("Cookie", "f=n; commontopbar_new_city_info=188%7C%E6%B2%88%E9%98%B3%7Csy; userid360_xml=84FDE92D1BAF969AF519B90812CFCF13; time_create=1567262816215; commontopbar_ipcity=sz%7C%E6%B7%B1%E5%9C%B3%7C0; f=n; commontopbar_new_city_info=188%7C%E6%B2%88%E9%98%B3%7Csy; id58=c5/nn10ylddQ114tnqyDAg==; 58home=sy; city=sy; 58tj_uuid=8e18e6a5-4c78-4b06-8864-9452f3cd3c91; new_uv=17; als=0; xxzl_deviceid=cWfZMCxyit2BPkbIU%2Bd8a8FdTbw2l9sEChCtC%2F7SrH85HAiC9SoYJ3chn9DmdGbN; wmda_uuid=076052445fe0f88b1f1c416e1906d6ff; wmda_new_uuid=1; wmda_visited_projects=%3B6333604277682; JSESSIONID=EBD0A3F1FF3D698F23646CDD0750350F; wmda_session_id_6333604277682=1565179314164-3182855a-6b3d-cce5; new_session=0; utm_source=; spm=; init_refer=; ppStore_fingerprint=A8406BD5518095CDDD4B89FB58ED8926DB3B2A1E581BE2E1%EF%BC%BF1565179316246; xzfzqtoken=TpUlsfohTpZGEJDxWRu09NfsVr5CxYoSFmINFidKGXo2iSq3ZFFcd4wqXp20EstCin35brBb%2F%2FeSODvMgkQULA%3D%3D")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("Referer", "https://sy.58.com/ershoufang/0/")
            .addHeader("Connection", "keep-alive")
            .addHeader("TE", "Trailers")
            .setRetryTimes(3)
            .setTimeOut(CommonConstants.TIME_OUT)
            .setSleepTime(3000)
            .setCycleRetryTimes(5);


    @Override
    public void process(Page page) {
        String url = page.getRequest().getUrl();
        System.out.println(url);
//        System.out.println(page.getHtml().get());
        if (url.indexOf("x.shtml") != -1) {
            // 正则有点弱，https://sy.58.com/ershoufang/十四位数字x.shtml?*
            log.info("详情页");
            HouseInfo58 model = Application.parse58Info(page.getHtml().get());
            // 放入model 让WBPipeline处理
            page.putField("model", model);
            // 放入html 让WBFilePipeline处理
            page.putField("html", page.getHtml().toString());

        } else if (StringUtils.startsWith(url, "https://sy.58.com/ershoufang/0/pn")) {
            log.info("列表页");
            // 列表页信息页很多，但是没有联系人电话及详情的描述，不过要爬取信息就可以直接拿来了
            Document document = page.getHtml().getDocument();
            List<String> urlList = new ArrayList<>(0);
            // 128条子数据
            Elements list_li = document.select("[class='house-list-wrap'] li");
            for (Element every : list_li) {
                Elements pic = every.select("[class=pic]").select("a");
                String href = pic.attr("href");
//                System.out.println(href);
                urlList.add(href);
            }
            String x = page.getHtml().xpath("/html/body/div[5]/div[5]/div[1]/ul/li[1]/div[2]/p[1]/span[1]").get();


            page.addTargetRequests(urlList);
        } else if (StringUtils.indexOf(page.getHtml().get(), "请输入验证码") != -1) {
            log.info("验证码限制" + url);
            page.addTargetRequest(url);
        } else if (page.getHtml().get().indexOf("你要找的页面不在这个星球上") != -1) {
            log.info("时间过长未找到页面" + url);
            page.setSkip(true);
        } else {
            log.error("未知页面" + url);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }
}

package com.personal.secondhand.processor;

import com.alibaba.fastjson.JSON;
import com.personal.secondhand.constants.CommonConstants;
import com.personal.secondhand.pipeline.FileInfoPipeline;
import com.personal.secondhand.pipeline.FilePagePipeline;
import com.personal.secondhand.util.ExcelUtil;
import com.personal.secondhand.util.JsoupUtils;
import com.personal.secondhand.vo.HouseInfo58;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/**
 * 五八页面抽取进程
 * 反爬限制还是比较多的。
 * 1）随机字体加密，每页的详情页面的字体都是随机字体，但seo优化meta的description标签中又包含了价格
 * 2）爬取限制，验证码。
 * 3）置顶的数据有多次重定向 需要获取到最终的页面路径
 */
@Slf4j
public class WBPageProcessor implements PageProcessor {

    private static Map<String, String> cookies = new HashMap<>(0);
    private static Site site = Site.me()
            .setUserAgent(CommonConstants.getRandomUserAgent())
            .setCharset(CommonConstants.ENCODING)
            .addHeader("Host", "sy.58.com")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
            .addHeader("Accept-Encoding", "gzip, deflate, br")
            .addHeader("DNT", "1")
            .addHeader("Cache-Control", "max-age=0")
            .addHeader("Cookie", "userid360_xml=84FDE92D1BAF969AF519B90812CFCF13; time_create=1567262816215; f=n; commontopbar_new_city_info=188%7C%E6%B2%88%E9%98%B3%7Csy; commontopbar_ipcity=sz%7C%E6%B7%B1%E5%9C%B3%7C0; id58=c5/nn10ylddQ114tnqyDAg==; 58home=sy; city=sy; 58tj_uuid=8e18e6a5-4c78-4b06-8864-9452f3cd3c91; new_uv=19; als=0; xxzl_deviceid=cWfZMCxyit2BPkbIU%2Bd8a8FdTbw2l9sEChCtC%2F7SrH85HAiC9SoYJ3chn9DmdGbN; wmda_uuid=076052445fe0f88b1f1c416e1906d6ff; wmda_new_uuid=1; wmda_visited_projects=%3B6333604277682; Hm_lvt_295da9254bbc2518107d846e1641908e=1565188906; JSESSIONID=00CD125CDA0A0A3F9447C5E4384138CB; wmda_session_id_6333604277682=1565534678037-aa587477-afc5-1abe; new_session=0; utm_source=; spm=; init_refer=; xzfzqtoken=I9cAkHNQ8%2FejJcgJTNTyXNBajtcEH%2BKBn%2F4UsvR67PvvaG22wlL4VJi%2BVLkPjV3Qin35brBb%2F%2FeSODvMgkQULA%3D%3D; f=n; commontopbar_new_city_info=188%7C%E6%B2%88%E9%98%B3%7Csy; commontopbar_ipcity=sz%7C%E6%B7%B1%E5%9C%B3%7C0; ppStore_fingerprint=A8406BD5518095CDDD4B89FB58ED8926DB3B2A1E581BE2E1%EF%BC%BF1565538031842")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("Referer", "https://sy.58.com/ershoufang/0/")
            .addHeader("Connection", "keep-alive")
            .addHeader("TE", "Trailers")
            .setRetryTimes(3)
            .setTimeOut(CommonConstants.TIME_OUT)
            .setSleepTime(5000)
            .setCycleRetryTimes(3);

    @Override
    public void process(Page page) {

        String url = page.getRequest().getUrl();
        String html = page.getHtml().get();

        if (html.indexOf("请输入验证码") != -1 || url.indexOf("target") != -1) {

            page.addTargetRequest(url);
        } else if (url.indexOf("x.shtml") != -1) {
            // 正则有点弱，https://sy.58.com/ershoufang/十四位数字x.shtml?*
            log.info("详情页");
            page.putField("infoHtml", page.getHtml().toString());

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
                if (href.indexOf("?target=") != -1) {
                    // 置顶页面会重定向302 几次
                    Map<String, String> cookies = site.getCookies();
                    href = getRedirectUrl(href, cookies);
//                    System.out.println(href);
                }
                urlList.add(href);
            }
            page.addTargetRequests(urlList);
            page.putField("pageHtml", html);
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

    /**
     * 置顶的数据url会重定向
     * 获取最终重定向地址
     *
     * @param url
     * @param cookies
     * @return
     */
    private String getRedirectUrl(String url, Map<String, String> cookies) {
        if (StringUtils.isBlank(url)) {
            return "";
        }
        try {
            Connection.Response response = JsoupUtils.connect(url).cookies(cookies).execute();
            if (response.statusCode() == 302) {
                String location = response.header("Location");
                return getRedirectUrl(location, cookies);
            } else if (response.statusCode() == 200) {
                return response.url().toString();
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return "";
    }

    /**
     * 程序入口
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // 第一步 先下载到本地html文件
        Spider spider = downloadHtmlFile();
        while (true) {
            if (spider.getStatus() == Spider.Status.Stopped) {
                //全下载完毕后再解析文件
                parseHtmlAndCreateExcel();
                break;
            }
        }
    }

    /**
     * 下载页面存储到本地路径
     *
     * @throws Exception
     */
    private static Spider downloadHtmlFile() throws Exception {
        String downloadPath = CommonConstants.DOWNLOAD_FILE_PATH + "/58html/";
        String url = "https://sy.58.com/ershoufang/0/pn";

        Connection.Response response = JsoupUtils.connect("https://sy.58.com/ershoufang/0/?ClickID=1").execute();
        cookies = response.cookies();
        for (Map.Entry<String, String> entrySet : cookies.entrySet()) {
            String key = entrySet.getKey();
            String value = entrySet.getValue();
            site.addCookie(key, value);
        }

        Spider spider = Spider.create(new WBPageProcessor());
        int start = 1;
        int end = 27;//27;
        // 加入url任务解析
        for (int i = start; i <= end; i++) {
            String pnUrl = url + i + "/?ClickID=1";
            spider.addUrl(pnUrl);
        }
        // 数据处理
        spider.addPipeline(new FileInfoPipeline(downloadPath));
        spider.addPipeline(new FilePagePipeline(downloadPath));
        // 启动线程爬取
        spider.thread((Runtime.getRuntime().availableProcessors() - 1) << 1);
        spider.run();
        return spider;
    }

    /**
     * 解析html后生成相关excel
     *
     * @throws Exception
     */
    public static void parseHtmlAndCreateExcel() throws Exception {

        String today = new DateTime().toString("yyyyMMdd");
        String downloadPath = CommonConstants.DOWNLOAD_FILE_PATH + File.separator + "58html" + File.separator + today + File.separator + "infoHtml" + File.separator;
        File file = new File(downloadPath);
        File[] files = file.listFiles();
        List<HouseInfo58> info58List = new ArrayList<>(0);
        for (File f : files) {
            String html = FileUtils.readFileToString(f, CommonConstants.ENCODING);
            HouseInfo58 info58 = HouseInfo58.parse58Info(html);
            info58List.add(info58);
        }

        List<String[]> dataList = new ArrayList<>(0);

        info58List.stream()
                .forEach(model -> {
                    dataList.add(new String[]{
                            model.getInfoUrl(),
                            model.getHeadTitle(),
                            model.getMetaDescription(),
                            model.getMetaCanonical(),
                            model.getPubDate(),
                            model.getNewInfo(),
                            model.getUpdateTime(),
                            model.getHouseNum(),
                            model.getTitle(),
                            model.getDownPayment(),
                            model.getTotalPrice(),
                            model.getPerSquare(),
                            model.getRoom(),
                            model.getArea(),
                            model.getToward(),
                            model.getFloor(),
                            model.getDecoration(),
                            model.getPropertyRight(),
                            model.getCommunity(),
                            model.getRegion(),
                            model.getPhoneNum(),
                            model.getGeneralDesc(),
                            JSON.toJSONString(model.getImgUrlList())
                    });
                });


        String[] title58 = new String[]{"详情页url", "title", "description", "规范url地址", "发布日期",
                "新上房源", "更新时间", "房源编号", "标题", "首付参考", "总价", "单价",
                "户型结构", "建筑面积", "户型朝向", "所属楼层", "装修情况", "产权情况",
                "小区", "位置", "联系方式", "概述信息", "图片url地址（jsonArray）"};

        XSSFWorkbook workbook = new XSSFWorkbook();
        ExcelUtil.make2007Excel(workbook, "58", title58, dataList);

        String todayString = new DateTime().toString("yyyyMMdd");
        File excel = new File("D:/58房源_" + todayString + ".xlsx");
        FileUtils.touch(excel);
        FileOutputStream output = FileUtils.openOutputStream(excel);

        workbook.write(output);
        output.flush();
        output.close();

        workbook.close();
    }

}

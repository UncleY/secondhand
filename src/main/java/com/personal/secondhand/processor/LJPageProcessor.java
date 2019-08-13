package com.personal.secondhand.processor;

import com.alibaba.fastjson.JSON;
import com.personal.secondhand.constants.CommonConstants;
import com.personal.secondhand.pipeline.FileInfoPipeline;
import com.personal.secondhand.pipeline.FilePagePipeline;
import com.personal.secondhand.util.ExcelUtil;
import com.personal.secondhand.util.JsoupUtils;
import com.personal.secondhand.vo.HouseInfoLJ;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 链家页面处理进程 列表页及详情页
 * 链家爬取相对很多
 */
@Slf4j
public class LJPageProcessor implements PageProcessor {


    @Override
    public void process(Page page) {
        String url = page.getUrl().get();
        String html = page.getHtml().get();
        if (StringUtils.indexOf(html, "没找到您访问的页面，可能原因") != -1) {
            log.error("页面已经过期{}", url);
            return;
        }
        if (StringUtils.indexOf(url, "/pg") != -1) {
            log.info("列表页 {}", url);
            // ul.sellListContent li.clear.LOGCLICKDATA div.info.clear div.title a
            Document document = JsoupUtils.parse(html);
            Elements elements = document.select("ul[class=sellListContent] li");
            for (Element ele : elements) {
                String infoUrl = ele.select("a[class=\"noresultRecommend img LOGCLICKDATA\"]").attr("href");
                page.addTargetRequest(infoUrl);
            }
            // 列表页面
            page.putField("pageHtml", html);
        } else if (page.getUrl().regex("https://sy\\.lianjia\\.com/ershoufang/[0-9]{12}.html").match()) {
            log.info("详情页 {}", url);
            page.putField("infoHtml", html);
        } else {
            log.error("未知页面 {}", url);
        }


    }

    @Override
    public Site getSite() {
        Site site = Site.me()
                .setCharset(CommonConstants.ENCODING)
                .setUserAgent(CommonConstants.getRandomUserAgent())
                .addHeader("Host", "sy.lianjia.com")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .addHeader("Accept-Encoding", "gzip, deflate, br")
                .addHeader("Referer", "https://sy.lianjia.com/ershoufang/co32/")
                .addHeader("Connection", "keep-alive")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("Cache-Control", "max-age=0")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .addHeader("Cookie", "TY_SESSION_ID=ce4b062e-5fd6-42a1-8348-0ff67c3ce731; lianjia_uuid=f22ef795-56f0-4929-a20b-b4ccefbb17ee; _jzqa=1.842514985700397600.1564312573.1565012195.1565457507.3; _jzqy=1.1564312573.1564312573.1.jzqsr=baidu.-; UM_distinctid=16c384b95a5ea-057cd9ccf041ff-4c312272-1fa400-16c384b95a6523; sensorsdata2015jssdkcross=%7B%22distinct_id%22%3A%2216c384b962e171-06f8c7952dc66-4c312272-2073600-16c384b962f8ae%22%2C%22%24device_id%22%3A%2216c384b962e171-06f8c7952dc66-4c312272-2073600-16c384b962f8ae%22%2C%22props%22%3A%7B%22%24latest_traffic_source_type%22%3A%22%E7%9B%B4%E6%8E%A5%E6%B5%81%E9%87%8F%22%2C%22%24latest_referrer%22%3A%22%22%2C%22%24latest_referrer_host%22%3A%22%22%2C%22%24latest_search_keyword%22%3A%22%E6%9C%AA%E5%8F%96%E5%88%B0%E5%80%BC_%E7%9B%B4%E6%8E%A5%E6%89%93%E5%BC%80%22%7D%7D; _smt_uid=5d3d83fd.1abc1d9c; _ga=GA1.2.1126247554.1564312576; Hm_lvt_9152f8221cb6243a53c83b956842be8a=1565012198,1565457515; CNZZDATA1255849613=523254511-1565009944-%7C1565457517; _qzja=1.944848892.1565012204957.1565012204957.1565457518826.1565457612820.1565457623675.0.0.0.14.2; CNZZDATA1254525948=218528258-1565008473-%7C1565455527; CNZZDATA1255633284=1258617953-1565009202-%7C1565452747; CNZZDATA1255604082=1568717441-1565011778-%7C1565456795; lianjia_ssid=321f2af5-72f3-46aa-a743-2728f8c4108b; select_city=440300; _jzqb=1.13.10.1565457507.1; _jzqc=1; _jzqckmp=1; _gid=GA1.2.1859789688.1565457514; Hm_lpvt_9152f8221cb6243a53c83b956842be8a=1565457624; all-lj=c32edd623b8a5a59c7de54c92107bb6c; _qzjb=1.1565457518826.11.0.0.0; _qzjc=1; _qzjto=11.1.0; srcid=eyJ0Ijoie1wiZGF0YVwiOlwiNzJmYWUwYTcyMTU2ZWFkZDZmOTg3YjU1YTNhOGVkN2Y0ZDQ4ZjMyNWI1YjgxMzA0OWZkOTU5NzQ0ZGM1YjQyZWY3NTk5OGNkYTc3MDU3MTA4YTIyMDU2NjgyOTRmNjQ0ZDViYzAyYmI4MjBjNzU5YmI5ODEwNzdjOTVjNDMxZTA1NTgxZGQ0Mzg0ZjE4YjI0ODIzZDUzYjVjMjA2NDRkOWY5YjA4Yjc5ODhjY2IxYWM0M2JhM2FlYjI0MjA0MmYyZWVlMjk2YjBjNzYwMmE0MzgwODE0MjIyY2JjNGUwNDQ3MGQwMjI2OTJhYWQzY2IwYmY1Yjk5MDY0ZWI0ODBlNGMwN2I0NTU5Y2NjNDZjZTBlZTM2OTFlMWFhN2Y1MjBjMjllOWIwMmNhMmNhZTljZDg4NDQ3YzMzZDIxMDZiMDJkMzNlYjJhOGQxMDNhY2JjYjJjZWYzM2U2NzE4MGRiZVwiLFwia2V5X2lkXCI6XCIxXCIsXCJzaWduXCI6XCI0M2YyNzVkN1wifSIsInIiOiJodHRwczovL3N5LmxpYW5qaWEuY29tL2Vyc2hvdWZhbmcvMTAyMTAxNzQ5ODQwLmh0bWwiLCJvcyI6IndlYiIsInYiOiIwLjEifQ==")
                .setRetryTimes(3)
                .setTimeOut(CommonConstants.TIME_OUT)
                .setCycleRetryTimes(3)
                .setSleepTime(CommonConstants.SLEEP_TIME);
        return site;
    }

    /**
     * 链家列表页面及详情页面存储本地
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Spider spider = downloadHtmlFile();
        while (true) {
            if (spider.getStatus() == Spider.Status.Stopped) {
                //全下载完毕后再解析文件
                parseHtmlAndCreateExcel();
                break;
            }
        }

    }

    private static Spider downloadHtmlFile() throws Exception {
        String downloadPath = CommonConstants.DOWNLOAD_FILE_PATH + "/ljhtml/pc/";

        Spider spider = Spider.create(new LJPageProcessor());
        int start = 1;
        int end = 100;
        for (int i = start; i <= end; i++) {
            spider.addUrl("https://sy.lianjia.com/ershoufang/pg" + i + "co32/");
        }
        spider.addPipeline(new FilePagePipeline(downloadPath));
        spider.addPipeline(new FileInfoPipeline(downloadPath));
        spider.thread((Runtime.getRuntime().availableProcessors() - 1) << 1);
        spider.run();
        return spider;
    }


    public static void parseHtmlAndCreateExcel() throws Exception {
        String today = new DateTime().toString("yyyyMMdd");
        String downloadPath = CommonConstants.DOWNLOAD_FILE_PATH + File.separator + "ljhtml" + File.separator + "pc" + File.separator + today + File.separator + "infoHtml" + File.separator;
        File file = new File(downloadPath);
        File[] files = file.listFiles();
        List<HouseInfoLJ> modelList = new ArrayList<>(0);
        for (File f : files) {
            String html = FileUtils.readFileToString(f, CommonConstants.ENCODING);
            HouseInfoLJ lj = HouseInfoLJ.parseByHtml(html);
            modelList.add(lj);
        }

        List<String[]> dataList = new ArrayList<>(0);
        modelList.stream()
                .forEach(model -> {
                    dataList.add(new String[]{
                            model.getInfoUrl(),
                            model.getMetaTitle(),
                            model.getHouseNum(),
                            model.getTitle(),
                            model.getTotalPrice(),
                            model.getPerSquare(),
                            model.getDownPayment(),
                            model.getRoom(),
                            model.getFloor(),
                            model.getToward(),
                            model.getDecoration(),
                            model.getArea(),
                            model.getBuildLife(),
                            model.getPropertyRight(),
                            model.getCommunity(),
                            model.getRegion(),
                            model.getPubDate(),
                            model.getTags(),
                            model.getHouseUse(),
                            model.getMortgageInformation(),
                            model.getHouseIntroduce(),
                            model.getTransOwnership(),
                            model.getLinkmanName(),
                            JSON.toJSONString(model.getImgUrlList())
                    });
                });


        String[] title = new String[]{"详情页url", "title", "房源编码", "标题", "总价",
                "单价", "首付参考", "户型结构", "所属楼层", "建筑信息","装修", "建筑面积", "建筑年限",
                "产权情况", "小区", "位置", "挂牌时间", "房源标签","房屋用途", "抵押信息", "房源特色",
                "交易权属", "联系人", "图片url地址（jsonArray）"};

        XSSFWorkbook workbook = new XSSFWorkbook();
        ExcelUtil.make2007Excel(workbook, "lianjia", title, dataList);

        String todayString = new DateTime().toString("yyyyMMdd");
        File excel = new File("D:/链接房源_" + todayString + ".xlsx");
        FileUtils.touch(excel);
        FileOutputStream output = FileUtils.openOutputStream(excel);

        workbook.write(output);
        output.flush();
        output.close();

        workbook.close();
    }
}

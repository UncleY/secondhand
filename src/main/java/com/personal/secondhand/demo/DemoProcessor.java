package com.personal.secondhand.demo;

import com.personal.secondhand.pipeline.FileInfoPipeline;
import com.personal.secondhand.pipeline.FilePagePipeline;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

/**
 * csdn论坛爬取demo
 */
public class DemoProcessor implements PageProcessor {

    /**
     * 加入一些F12 debug到的请求头信息组装
     */
    private Site site = Site.me()
            .addHeader("F12查到的头信息key", "信息值")
            .setUserAgent("伪装的useragent")
            .setSleepTime(3000)
            .setRetryTimes(3)
            .setCycleRetryTimes(3);

    @Override
    public void process(Page page) {
        String html = page.getHtml().get();
        String url = page.getUrl().get();
        if (url.indexOf("page") != -1) {
            System.out.println("列表页");
            // 使用jsoup 解析html内容 并分析出每个帖子的详情链接
            Document document = Jsoup.parse(html);
            Elements elements = document.select("a[class=forums_title]");
            for (Element ele : elements) {
                String infoUrl = ele.attr("href");
                // 将列表页的详情url添加至任务中继续处理
                page.addTargetRequest(infoUrl);
            }
            // 将获取到的列表页html内容交由FilePagePipeline数据处理里
            page.putField("pageHtml",html);
        } else {
            System.out.println("详情页");
            // 将获取到的详情页html内容交由FileInfoPipeline数据处理里
            page.putField("infoHtml",html);
        }

    }

    public static void main(String[] args) {
        // 创建一个任务 处理
        Spider spider = Spider.create(new DemoProcessor());
        // 多个任务url
        spider.addUrl("https://bbs.csdn.net/forums/J2EE?page=1");
        spider.addUrl("https://bbs.csdn.net/forums/J2EE?page=2");
        // 将页面解析后的数据交给FileInfoPipeline/FilePagePipeline处理
        spider.addPipeline(new FileInfoPipeline("d:/csdnhtml/"));
        spider.addPipeline(new FilePagePipeline("d:/csdnhtml/"));
        // 开启多个线程
        spider.thread(4);
        // 启动
        spider.run();
    }


    @Override
    public Site getSite() {
        return site;
    }
}

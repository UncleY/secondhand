package com.personal.secondhand.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.FilePipeline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * 公共的详情页面下载到本地
 * 只要接收到infoHtml即生成列表html页面
 * 文件命名替换（原命名可能无法找到具体访问url）：
 *  （1）去除https://
 *  （2）?替换成#
 *  （3）/替换成_
 */
@Slf4j
public class FileInfoPipeline extends FilePipeline {

    public FileInfoPipeline() {
        super();
    }

    public FileInfoPipeline(String path) {
        super(path);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        // 从PageProcess获取设置的html内容
        String html = resultItems.get("infoHtml");
        if (StringUtils.isBlank(html)) {
            // 没有就跳出
            return;
        }
        String url = resultItems.getRequest().getUrl();
        url = url.replaceAll("https://", "").replaceAll("\\?", "#").replaceAll("/", "_");
        // 文件命名为url路径，替换文件命名不符合的情况后形式如：
        // 以下内容参考至FilePipeline#process 替换了写入内容
        String today = new DateTime().toString("yyyyMMdd");
        String path = super.path + PATH_SEPERATOR + today + PATH_SEPERATOR + "infoHtml" + PATH_SEPERATOR + url;
        try {
//            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.getFile(path + DigestUtils.md5Hex(resultItems.getRequest().getUrl()) + ".html")), "UTF-8"));
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.getFile(path + ".html")), "UTF-8"));
            printWriter.println(html);
            printWriter.close();
        } catch (IOException e) {
            log.error("info html文件写入异常", e);
        }
    }
}
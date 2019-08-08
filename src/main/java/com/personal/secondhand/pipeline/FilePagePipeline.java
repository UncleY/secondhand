package com.personal.secondhand.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.FilePipeline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

@Slf4j
public class FilePagePipeline extends FilePipeline {
    public FilePagePipeline() {
        super();
    }

    public FilePagePipeline(String path) {
        super(path);
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        // 从PageProcess获取设置的html内容
        String html = resultItems.get("pageHtml");
        if (StringUtils.isBlank(html)) {
            // 没有就跳出
            return;
        }
        String url = resultItems.getRequest().getUrl();
        url = url.replaceAll("https://", "").replaceAll("\\?", "#").replaceAll("/", "_");
        System.out.println(url);
        // 以下内容参考至FilePipeline#process 替换了写入内容
        String path = super.path + PATH_SEPERATOR + "pageHtml" + PATH_SEPERATOR + url ;
        try {
//            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.getFile(path + DigestUtils.md5Hex(resultItems.getRequest().getUrl()) + ".html")), "UTF-8"));
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.getFile(path + ".html")), "UTF-8"));
            printWriter.println(html);
            printWriter.close();
        } catch (IOException e) {
            log.error("page html文件写入异常", e);
        }
    }
}

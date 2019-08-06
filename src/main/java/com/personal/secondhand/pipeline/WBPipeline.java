package com.personal.secondhand.pipeline;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.personal.secondhand.vo.HouseInfo58;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class WBPipeline implements Pipeline {

    private static AtomicInteger atomicInteger = new AtomicInteger();
    private static JSONObject jsonObject = new JSONObject();

    @Override
    public void process(ResultItems resultItems, Task task) {
        HouseInfo58 model = resultItems.get("model");
        jsonObject.put("" + atomicInteger.getAndIncrement(), model);
        try {
            File file = new File("d:/ershoufang.json");
            FileUtils.touch(file);
            FileUtils.writeLines(file, Lists.newArrayList(jsonObject.toJSONString()), true);
        } catch (Exception e) {
            log.error("",e);
        }
    }
}

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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 详情页数据解析成model
 * 追加至相关存储地方
 */
@Slf4j
public class WBPipeline implements Pipeline {

    private static AtomicInteger atomicInteger = new AtomicInteger();

    @Override
    public void process(ResultItems resultItems, Task task) {
        HouseInfo58 model = resultItems.get("model");
        if(Objects.isNull(model)){
            return;
        }
        log.info(model.toString());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("" + atomicInteger.getAndIncrement(), model);
        try {
            File file = new File("d:/secondhand/58.json");
            FileUtils.touch(file);
            FileUtils.writeLines(file, Lists.newArrayList(jsonObject.toJSONString()), true);
        } catch (Exception e) {
            log.error("",e);
        }
    }
}

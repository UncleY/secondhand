package com.personal.secondhand.vo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.personal.secondhand.util.JsoupUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 58房源 提取信息 映射vo
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseInfo58 implements Serializable {
    //////////////////页面信息///////////////
    /**
     * 详情页面url地址
     */
    private String infoUrl;
    /**
     * header title
     */
    private String headTitle;
    /**
     * meta description
     */
    private String metaDescription;
    /**
     * meta 规范源地址
     */
    private String metaCanonical;
    /**
     * 发布日期（可能是实际的发布日期）
     */
    private String pubDate;


    ////////////房源信息//////////////
    /**
     * 是否新上房源
     */
    private String newInfo;
    /**
     * 更新时间
     */
    private String updateTime;
    /**
     * 房源编号（不知道是房本号 还是58自己的编号）
     */
    private String houseNum;
    /**
     * 标题
     */
    private String title;
    /**
     * 首付参考
     */
    private String downPayment;
    /**
     * 总价
     */
    private String totalPrice;
    /**
     * 每平米花费
     */
    private String perSquare;
    /**
     * 户型结构 如：1室1厅1卫 高层/共7层
     */
    private String room;
    /**
     * 建筑面积 如：43平  精装修
     */
    private String area;
    /**
     * 建筑信息 如： 南北  建筑年代(暂无信息)
     */
    private String toward;
    /**
     * 所属楼层
     */
    private String floor;
    /**
     * 装修
     */
    private String decoration;
    /**
     * 产权情况
     */
    private String propertyRight;

    /**
     * 小区：五三小区双学区岐山一校 43
     */
    private String community;
    /**
     * 位置：皇姑
     */
    private String region;

    /**
     * 联系方式
     */
    private String phoneNum;
    /**
     * 概述信息
     */
    private String generalDesc;

    /**
     * 图片url地址
     */
    private List<String> imgUrlList = new ArrayList<>(0);

    /**
     * 根据html详情页面解析成vo内容
     *
     * @param html
     * @return
     */
    public static HouseInfo58 parse58Info(String html) {
        Document document = JsoupUtils.parse(html);
//        System.out.println("######head title########################");
        String headTitle = document.title();
        String newInfo = document.select("html body div.main-wrap div.house-title p.house-update-info span.ts").text();
        String updateTime = document.select("html body div.main-wrap div.house-title p.house-update-info span.up").text();
//        String totalCount = document.select("html body div.main-wrap div.house-title p.house-update-info span.up").text();
        String canonical = document.select("html head link[rel=canonical]").attr("href");
//        System.out.println("######meta content############");
        String content = document.select("html head meta[name='description']").attr("content");

        String totalPrice = StringUtils.substring(content, content.indexOf("售价："), content.indexOf("；"));
        String perSquare = StringUtils.substring(content, content.indexOf("（") + 1, content.indexOf("）"));

//        System.out.println("######title ############");
        String title = document.select("div[class=house-title] h1[class=c_333 f20]").text();
//        System.out.println("#######房子结构信息 generalSituation###########");
        String room = document.select("div[id=generalSituation] ul[class=general-item-left] li:eq(1) span[class=c_000]").text();
        String area = document.select("div[id=generalSituation] ul[class=general-item-left] li:eq(2) span[class=c_000]").text();
        String toward = document.select("div[id=generalSituation] ul[class=general-item-left] li:eq(3) span[class=c_000]").text();

        String floor = document.select("div[id=generalSituation] ul[class=general-item-right] li:eq(0) span[class=c_000]").text();
        String decoration = document.select("div[id=generalSituation] ul[class=general-item-right] li:eq(1) span[class=c_000]").text();
        String propertyRight = document.select("div[id=generalSituation] ul[class=general-item-right] li:eq(2) span[class=c_000]").text();

        String community = document.select("ul[class=house-basic-item3] li:eq(0) span:eq(1)").text();
        String region = document.select("ul[class=house-basic-item3] li:eq(1) span:eq(1)").text();

//        System.out.println("##########概述信息 generalDesc############");
        String generalDesc = document.select("div[id=generalDesc] div[class=genaral-pic-desc]:eq(0) p[class=pic-desc-word]").text();
        String houseNum = document.select("div[id=generalDesc] div[class=genaral-pic-desc]:eq(1) p[class=pic-desc-word]").text();
//        System.out.println("#######联系方式############");
        String phoneNum = document.select("[class=phone-num]").text();
//        System.out.println("########首付参考##############");
        String downPayment = document.select("div[id=generalExpense] ul[class=general-item-right] li span[class=c_000]").text();
//        System.out.println("########（可能）发布日期##iso 8601格式 没带时区 可能默认是北京时间+08:00###############");
        String ldjson = document.select("script[type=application/ld+json]").html();
        JSONObject jsonObject = JSON.parseObject(ldjson);
        String pubDate = jsonObject.getString("pubDate");

//        System.out.println("########小图url地址##############");
        List<String> imgUrlList = new ArrayList<>(0);
        Elements elements = document.select("ul[id=smallPic] li");
        for (Element ele : elements) {
            String imgUrl = ele.attr("data-value");
            if (StringUtils.isNotBlank(imgUrl)) {
                imgUrlList.add(imgUrl);
            }
        }


        HouseInfo58 model = HouseInfo58.builder()
                .infoUrl(canonical)
                .headTitle(headTitle)
                .metaDescription(content)
                .metaCanonical(canonical)
                .pubDate(pubDate)
                .newInfo(newInfo)
                .updateTime(updateTime)
                .houseNum(houseNum)
                .title(title)
                .downPayment(downPayment)
                .totalPrice(totalPrice)
                .perSquare(perSquare)
                .room(room)
                .area(area)
                .toward(toward)
                .floor(floor)
                .decoration(decoration)
                .propertyRight(propertyRight)
                .community(community)
                .region(region)
                .phoneNum(phoneNum)
                .generalDesc(generalDesc)
                .imgUrlList(imgUrlList)
                .build();
//        log.info(model.toString());
        return model;
    }

}

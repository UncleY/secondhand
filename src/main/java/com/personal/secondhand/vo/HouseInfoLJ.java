package com.personal.secondhand.vo;

import com.personal.secondhand.util.JsoupUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 链家房源信息 映射vo
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseInfoLJ implements Serializable {
    /**
     * 详情页
     */
    private String infoUrl;
    /**
     * html title
     */
    private String metaTitle;
    /**
     * 房源编码
     */
    private String houseNum;
    /**
     * 标题
     */
    private String title;


    /**
     * 总价
     */
    private String totalPrice;
    /**
     * 单价
     */
    private String perSquare;
    /**
     * 首付参考
     */
    private String downPayment;
    /**
     * 户型结构 如：1室1厅1卫 高层/共7层
     */
    private String room;
    /**
     * 所属楼层
     */
    private String floor;
    /**
     * 建筑信息 如： 南北  建筑年代(暂无信息)
     */
    private String toward;
    /**
     * 装修
     */
    private String decoration;
    /**
     * 建筑面积 如：43平  精装修
     */
    private String area;
    /**
     * 建筑年限
     */
    private String buildLife;
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
     * 挂牌时间
     */
    private String pubDate;
    /**
     * 房源标签
     */
    private String tags;
    /**
     * 房屋用途
     */
    private String houseUse;
    /**
     * 抵押信息
     */
    private String mortgageInformation;
    /**
     * 房源特色
     */
    private String houseIntroduce;

    /**
     * 交易权属
     */
    private String transOwnership;
    /**
     * 联系人名称 联系人电话
     */
    private String linkmanName;

    /**
     * 图片地址
     */
    private List<String> imgUrlList = new ArrayList<>(0);

    /**
     * html解析成对象
     *
     * @param html
     * @return
     * @throws Exception
     */
    public static HouseInfoLJ parseByHtml(String html) throws Exception {
        Document document = JsoupUtils.parse(html);

        String metaTitle = document.title();

        String title = document.select("div[class=\"sellDetailHeader\"] div[class=\"title-wrapper\"] div[class=\"content\"] div[class=\"title\"]").text();

        String houseNum = document.select("div[class=\"btnContainer  LOGVIEWDATA LOGVIEW\"]").attr("data-lj_action_resblock_id");

        Elements introContent = document.select("div[id=introduction] div div[class=\"introContent\"] div[class=\"base\"] div[class=\"content\"] ul li");
        for (Element ele : introContent) {
            // 去除span标签
            ele.select("span").remove();
        }

//        String room = content.select("div[class=\"houseInfo\"] div[class=\"room\"] div[class=\"mainInfo\"]").text();
        String room = introContent.eq(0).text();
//        String floor = content.select("div[class=\"houseInfo\"] div[class=\"room\"] div[class=\"subInfo\"]").text();
        String floor = introContent.eq(1).text();
        String propertyRight = introContent.eq(12).text();


        String pubDate = document.select("div[class=\"transaction\"] div[class=\"content\"] ul li:eq(0) span:eq(1)").text();
        String transOwnership = document.select("div[class=\"transaction\"] div[class=\"content\"] ul li:eq(1) span:eq(1)").text();
        String houseUse = document.select("div[class=\"transaction\"] div[class=\"content\"] ul li:eq(3) span:eq(1)").text();
        String mortgageInformation = document.select("div[class=\"transaction\"] div[class=\"content\"] ul li:eq(6) span:eq(1)").text();

        Elements content = document.select("div[class=\"overview\"] div[class=\"content\"]");

        String total = content.select("div[class=\"price\"] span[class=\"total\"]").text();
        String unit = content.select("div[class=\"price\"] span[class=\"unit\"]").text();
        String totalPrice = total + unit;


        String perSquare = content.select("div[class=\"price\"] div[class=\"text\"] div[class=\"unitPrice\"]").text();

        String comment = content.select("div[class=\"price\"] div[class=\"text\"]").html();
        comment = comment.replaceAll("<!--", "").replaceAll("-->", "");
        String downPayment = JsoupUtils.parse(comment).select("div[class=tax] span[class=\"taxtext\"]").text();


        String toward = content.select("div[class=\"houseInfo\"] div[class=\"type\"] div[class=\"mainInfo\"]").text();
        String decoration = content.select("div[class=\"houseInfo\"] div[class=\"type\"] div[class=\"subInfo\"]").text();

        String area = content.select("div[class=\"houseInfo\"] div[class=\"area\"] div[class=\"mainInfo\"]").text();
        String buildLife = content.select("div[class=\"houseInfo\"] div[class=\"area\"] div[class=\"subInfo\"]").text();


        String community = content.select("div[class=\"aroundInfo\"] div[class=\"communityName\"] a").text();
        community = community.replace(" 地图", "");
        String region = content.select("div[class=\"aroundInfo\"] div[class=\"areaName\"] span[class=\"info\"]").text();


        Elements eleTags = document.select("div[class=\"introContent showbasemore\"] div[class=\"tags clear\"] div[class=\"content\"] a");
        String tags = "";
        for (Element ele : eleTags) {
            tags += ele.text() + ";";
        }


        Elements eleHouseIntro = document.select("div[class=baseattribute clear]");
        String houseIntroduce = "";
        for (Element ele : eleHouseIntro) {
            houseIntroduce += ele.text() + ";";
        }


        List<String> imgList = new ArrayList<>(0);
        Elements elements = document.select("div[id=\"thumbnail2\"] ul[class=\"smallpic\"] li");
        for (Element ele : elements) {
            String imgUrl = ele.attr("data-src");
            imgList.add(imgUrl);
        }

        String linkman = document.select("div[class=\"agent-tips\"] div[class=\"fr\"]").text();
        linkman = linkman.replace("/房源维护人", "");
        if (StringUtils.isBlank(linkman)) {
            linkman = document.select("div[class=\"brokerInfoText fr LOGVIEWDATA\"] div[class=\"brokerName\"] a").text();
            String linkmanPhone = document.select("div[class=\"brokerInfoText fr LOGVIEWDATA\"] div[class=\"phone\"]").text();
            linkmanPhone = linkmanPhone.replace("微信扫码拨号", "");
            linkman = linkman + " " + linkmanPhone;
        }

        HouseInfoLJ model = HouseInfoLJ.builder()
                .infoUrl("https://sy.lianjia.com/ershoufang/" + houseNum + ".html")
                .metaTitle(metaTitle)
                .houseNum(houseNum)
                .totalPrice(totalPrice)
                .title(title)
                .totalPrice(totalPrice)
                .perSquare(perSquare)
                .downPayment(downPayment)
                .room(room)
                .floor(floor)
                .toward(toward)
                .decoration(decoration)
                .area(area)
                .buildLife(buildLife)
                .propertyRight(propertyRight)
                .community(community)
                .region(region)
                .pubDate(pubDate)
                .tags(tags)
                .transOwnership(transOwnership)
                .houseUse(houseUse)
                .mortgageInformation(mortgageInformation)
                .houseIntroduce(houseIntroduce)
                .linkmanName(linkman)

                .imgUrlList(imgList)
                .build();
//        log.info(model.toString());
        return model;
    }

    public static void main(String[] args) throws Exception {
        File file = new File("D:\\spider\\ljhtml\\pc\\20190813\\infoHtml\\sy.lianjia.com_ershoufang_102100831577.html.html");
        String html = FileUtils.readFileToString(file, "utf-8");
        parseByHtml(html);
    }

}

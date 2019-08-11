package com.personal.secondhand.vo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.personal.secondhand.constants.CommonConstants;
import com.personal.secondhand.util.ExcelUtil;
import com.personal.secondhand.util.JsoupUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 芒果房源信息 映射vo
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HouseInfo517CN implements Serializable {

    public static final String INFO_URL_PREFIX = "https://www.517.cn/ershoufang/";
    public static final String GET_LINKMAN_PHONE_API_URL = "https://www.517.cn/Ashx/userCenter/Account.ashx?action=Getmobile&uid=";
    /**
     * url
     */
    private String infoUrl;
    /**
     * meta title
     */
    private String metaTitle;
    /**
     * 房源编号
     */
    private String houseNum;
    /**
     * 税标签
     */
    private String taxTags;
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
     * 户型结构
     */
    private String room;
    /**
     * 建筑面积
     */
    private String area;
    /**
     * 建筑年限
     */
    private String buildLife;
    /**
     * 朝向
     */
    private String toward;
    /**
     * 所属楼层
     */
    private String floor;
    /**
     * 装修情况
     */
    private String decoration;
    /**
     * 小区名称
     */
    private String community;
    /**
     * 小区地址
     */
    private String address;
    /**
     * 小区均价
     */
    private String avgPrice;
    /**
     * 房源信息介绍
     */
    private String houseMemo;
    /**
     * 联系人id
     */
    private String linkmanId;
    /**
     * 联系人名称
     */
    private String linkmanName;
    /**
     * 联系人电话
     */
    private String linkmanPhone;
    /**
     * 联系人从业年限
     */
    private String linkmanExp;

    private List<String> imgUrlList = new ArrayList<>(0);

    /**
     * 解析html 获取详情内容实体
     *
     * @param html
     * @return
     */
    public static HouseInfo517CN initByHtml(String html) {
        if (StringUtils.isBlank(html)) {
            return null;
        }
        Document document = JsoupUtils.parse(html);
        String headTitle = document.title();
        String houseNum = document.select("div[id=ContentPlaceHolder1__pHouseTags] i").text();
        if (StringUtils.isNotBlank(houseNum)) {
            houseNum = houseNum.replaceAll("房源编号：", "");
        }
        String taxTags = document.select("em[class=\"tags-ms\"]").text();
        String title = document.select("span[id=ContentPlaceHolder1__labTitle]").text();
        String totalPrice = document.select("div[class=nfyzongjia]").text();
        String perSquare = document.select("div[class=nfydanjia] span").text();

        String room = document.select("div[class=\"nfyxxdisplay huxingloucheng\"] span").text();
        String floor = document.select("div[class=\"nfyxxdisplay huxingloucheng\"] i").text();

        String toward = document.select("div[class=\"nfyxxdisplay chaoxiangzhuangxiu\"] span").text();
        String buildLife = document.select("div[class=\"nfyxxdisplay chaoxiangzhuangxiu\"] i").text();

        String area = document.select("div[class=\"nfyxxdisplay mianjifangling\"] span").text();
        String decoration = document.select("div[class=\"nfyxxdisplay mianjifangling\"] i").text();

        String avgPrice = document.select("div[class=\"nfangyanxiaoqu clearfix\"] ul li:eq(0)").text();
        String community = document.select("div[class=\"nfangyanxiaoqu clearfix\"] ul li:eq(1) a:eq(0)").text();
        String address = document.select("div[class=\"nfangyanxiaoqu clearfix\"] ul li:eq(2) a:eq(0)").text();


        String linkmanId = document.select("li[class=m-jjr-name] a").attr("href");
        if (StringUtils.isNotBlank(linkmanId)) {
            linkmanId = linkmanId.replaceAll("https://www.517.cn/s/", "");
        }
        String linkmanName = document.select("li[class=m-jjr-name] a").text();

        String linkmanExp = document.select("div[id=\"_jiaMengNotShowRenZhengDiv\"]").text();

        String linkmanPhone = "";
        try {
            String getPhoneApiUrl = GET_LINKMAN_PHONE_API_URL + linkmanId;
            System.out.println(getPhoneApiUrl);
            String json = JsoupUtils.connect(getPhoneApiUrl)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .ignoreContentType(true)
                    .execute().body();
            linkmanPhone = (String) JSONPath.read(json, "$.data.data.mobile");
            // 虽然没限制api 但最好还是限制频率
            Thread.sleep(1500);
        } catch (Exception e) {
            log.error("获取联系人电话失败", e);
        }

        String houseMemo = document.select("div[class=\"contentBox\"]").text();

        Elements elements = document.select("a[class=imga]");
        List<String> imgList = new ArrayList<>(0);
        for (Element ele : elements) {
            String imgUrl = ele.attr("href");
            imgList.add(imgUrl);
        }


        HouseInfo517CN model = HouseInfo517CN.builder()
                .infoUrl(INFO_URL_PREFIX + houseNum)
                .metaTitle(headTitle)
                .houseNum(houseNum)
                .taxTags(taxTags)
                .title(title)
                .totalPrice(totalPrice)
                .perSquare(perSquare)
                .room(room)
                .area(area)
                .buildLife(buildLife)
                .toward(toward)
                .floor(floor)
                .decoration(decoration)
                .community(community)
                .address(address)
                .avgPrice(avgPrice)
                .houseMemo(houseMemo)
                .linkmanId(linkmanId)
                .linkmanName(linkmanName)
                .linkmanPhone(linkmanPhone)
                .linkmanExp(linkmanExp)
                .imgUrlList(imgList)
                .build();


        log.info(model.toString());
        return model;
    }


    public static void main(String[] args) throws Exception {

        // 存储详情的html的路径地址
        File file = new File("D:\\517html\\pc\\20190811\\infoHtml\\");
        File[] files = file.listFiles();
        List<HouseInfo517CN> info58List = new ArrayList<>(0);
        for (File f : files) {
            String html = FileUtils.readFileToString(f, CommonConstants.ENCODING);
            HouseInfo517CN info = initByHtml(html);
            info58List.add(info);
        }

        List<String[]> dataList = new ArrayList<>(0);

        info58List.stream()
                .forEach(model -> {
                    dataList.add(new String[]{
                            model.getInfoUrl(),
                            model.getMetaTitle(),
                            model.getHouseNum(),
                            model.getTaxTags(),
                            model.getTitle(),
                            model.getTotalPrice(),
                            model.getPerSquare(),
                            model.getRoom(),
                            model.getArea(),
                            model.getBuildLife(),
                            model.getToward(),
                            model.getFloor(),
                            model.getDecoration(),
                            model.getCommunity(),
                            model.getAddress(),
                            model.getAvgPrice(),
                            model.getHouseMemo(),
                            model.getLinkmanId(),
                            model.getLinkmanName(),
                            model.getLinkmanPhone(),
                            model.getLinkmanExp(),
                            JSON.toJSONString(model.getImgUrlList())
                    });
                });


        String[] title58 = new String[]{"详情页url", "title", "房源编号", "标签", "标题", "总价", "单价",
                "户型结构", "建筑面积", "建筑年限", "户型朝向",
                "所属楼层", "装修情况", "小区名称", "小区地址", "小区均价",
                "房源信息介绍", "联系人id", "联系人名称", "联系人电话", "联系人从业年限", "图片url地址（jsonArray）"};

        XSSFWorkbook workbook = new XSSFWorkbook();
        ExcelUtil.make2007Excel(workbook, "517", title58, dataList);

        String todayString = new DateTime().toString("yyyyMMdd");
        File excel = new File("D:/ershoufang" + todayString + ".xlsx");
        FileUtils.touch(excel);
        FileOutputStream output = FileUtils.openOutputStream(excel);

        workbook.write(output);
        output.flush();
        output.close();

        workbook.close();
    }

}

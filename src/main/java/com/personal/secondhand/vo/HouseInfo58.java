package com.personal.secondhand.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
     * 图片url地址
     */
    private List<String> imgUrlList = new ArrayList<>(0);

}

package com.personal.secondhand;

import com.personal.secondhand.util.JsoupUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangrui on 2019-8-1.
 *
 * @author yangrui
 */
public class Application {


    public static void main(String[] args) throws Exception {

        List<String> list58 = get58ListUrl();


        String url = list58.get(list58.size() / 2 + 1);

        String html = JsoupUtils.connect(url).get().html();
        System.out.println(html);

        // 加密字体需 加载font

//
//        Document document = Jsoup.connect(_58url).get();
//
//        Elements div_ul = document.select("[class=house-list-wrap]");
//        Elements list_li = div_ul.select("li");
//        for (Element every : list_li) {
//            Elements pic = every.select("[class=pic]").select("a");
//            String href = pic.attr("href");
//            String imgSrc = pic.select("img").attr("data-src");
////            System.out.println(href);
////            System.out.println(imgSrc);
//            Elements info = every.select("[class=list-info]");
//            Elements jjrinfo = every.select("[class=jjrinfo]");
//            Elements price = every.select("[class=jjrinfo]");
//            System.out.println("############");
//            break;
//        }
//        String html = document.html();
//        System.out.println(html);
    }

    /**
     * 获取58列表数据 每个详情页url
     *
     * @return
     * @throws Exception
     */
    private static List<String> get58ListUrl() throws Exception {
        List<String> urlList = new ArrayList<>(0);
        String url = "https://sy.58.com/ershoufang/0";
        Document document = JsoupUtils.connect(url).get();
        Elements div_ul = document.select("[class=house-list-wrap]");
        Elements list_li = div_ul.select("li");
        for (Element every : list_li) {
            Elements pic = every.select("[class=pic]").select("a");
            String href = pic.attr("href");
            System.out.println(href);
            urlList.add(href);
        }
        return urlList;
    }


}

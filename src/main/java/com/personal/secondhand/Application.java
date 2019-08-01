package com.personal.secondhand;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by yangrui on 2019-8-1.
 *
 * @author yangrui
 */
public class Application {


    private static final String _58url = "https://sy.58.com/ershoufang";

    public static void main(String[] args) throws Exception {

        Document document = Jsoup.connect(_58url).get();

        Elements div_ul = document.select("[class=house-list-wrap]");
        Elements list_li  = div_ul.select("li");
        for (Element every : list_li) {
            Elements pic = every.select("[class=pic]").select("a");
            String href = pic.attr("href");
            String imgSrc = pic.select("img").attr("data-src");
            System.out.println(href);
            System.out.println(imgSrc);
            Elements info = every.select("[class=list-info]");
            Elements jjrinfo = every.select("[class=jjrinfo]");
            Elements price = every.select("[class=jjrinfo]");
            System.out.println("############");
            break;
        }
//        String html = document.html();
//        System.out.println(html);


    }

}

package com.personal.secondhand;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.personal.secondhand.constants.CommonConstants;
import com.personal.secondhand.util.ExcelUtil;
import com.personal.secondhand.util.JsoupUtils;
import com.personal.secondhand.vo.HouseInfo58;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;

/**
 * Created by yangrui on 2019-8-1.
 *
 * @author yangrui
 */
@Log
public class Application {
    /**
     * 爬取去重
     */
    private static CopyOnWriteArraySet<String> urlCOWSet = new CopyOnWriteArraySet<>();

    /**
     * 存储到本地的列表html内容的文件名称前缀
     */
    private static final String FILE_NAME_58_LIST_HTML = "58ListHtml_";
    /**
     * 存储到本地的详情html内容的文件名称前缀
     */
    private static final String FILE_NAME_58_INFO_HTML = "58InfoHtml_";
    /**
     * 存储关联关系的json文件名称
     */
    private static final String FILE_NAME_58_LIST_JSON = "58ListJson";
    /**
     * html文件名后缀
     */
    private static final String FILE_NAME_SUFFIX = ".html";


    /**
     * 有点思路了，先列表url不用存储。将每一页的html都存储起来
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // 第一步 缓存本地列表页面 每页有128条数据
        create58InfoListFile();

        // 第二步 解析每个列表页面内容，并生成映射关系（方便制造refer内容）
        parseAllListFile();

        // 第三部 从第二步的映射关系 爬取并生成缓存详情文件
        parseAllInfo();

        // 第四步 获取全部第三步生成的详情缓存文件进行解析
        List<HouseInfo58> info58List = parseAll58Info();

        // 第五步 生成excel内容导出
        List<String[]> dataList = new ArrayList<>(0);
        info58List.stream().parallel()
                .forEach(model -> {
                    dataList.add(new String[]{
                            model.getInfoUrl(),
                            model.getHeadTitle(),
                            model.getMetaDescription(),
                            model.getMetaCanonical(),
                            model.getPubDate(),
                            model.getHouseNum(),
                            model.getTitle(),
                            model.getDownPayment(),
                            model.getTotalPrice(),
                            model.getPerSquare(),
                            model.getRoom(),
                            model.getArea(),
                            model.getToward(),
                            model.getFloor(),
                            model.getDecoration(),
                            model.getPropertyRight(),
                            model.getCommunity(),
                            model.getRegion(),
                            model.getPhoneNum(),
                            JSON.toJSONString(model.getImgUrlList())
                    });
                });


        String[] title58 = new String[]{"详情页url", "title", "description", "规范url地址", "发布日期", "房源编号", "标题", "首付参考", "总价", "单价", "户型结构", "建筑面积", "户型朝向", "所属楼层", "装修情况", "产权情况", "小区", "位置", "联系方式", "图片url地址（jsonArray）"};

        XSSFWorkbook workbook = ExcelUtil.make2007Excel("58", title58, dataList);

        String todayString = new DateTime().toString("yyyyMMdd_HHmmss");
        File file = new File("D:/ershoufang" + todayString + ".xlsx");
        FileUtils.touch(file);
        FileOutputStream output = FileUtils.openOutputStream(file);

        workbook.write(output);
        output.flush();
        output.close();

        workbook.close();
    }

    /**
     * 获取58所有列表的数据存入到文件中 （在本地解析少于 爬取过载的限制）
     *
     * @throws Exception
     */
    private static void create58InfoListFile() throws Exception {
        Set<String> pnList = new LinkedHashSet<>(0);

        // 获取存储路径
        File _58Path = get58File();
        String parentPath = _58Path.getPath();
        Map<String, String> cookies = new HashMap<>(0);

        // 总共的27页 但不是全部数据， 只是显示7月-31日的  可能就是只给出一个月的数据而已
        int _58TotalPage = 27;
        for (int i = 1; i <= _58TotalPage; i++) {
            String pnUrl = "https://sy.58.com/ershoufang/0/pn" + i + "/?ClickID=1";
            if (pnList.add(pnUrl)) {

                // 每个列表页html内容存储到文件里 文件名为url
                System.out.println(pnUrl);
                Connection connection = JsoupUtils.connect(pnUrl).referrer("https://sy.58.com/?PGTID=0d40000c-02cf-b884-4f20-a9d4b1daa7bf&ClickID=1");
                Connection.Response response = connection.execute();
                // 获取cookies
                cookies = response.cookies();
                // 获取 html
                String html = response.body();

                // 存储文件内容至class path下
                File file = new File(parentPath + "/" + FILE_NAME_58_LIST_HTML + i + FILE_NAME_SUFFIX);
                FileUtils.touch(file);
                FileUtils.writeStringToFile(file, html, CommonConstants.ENCODING);

                // 随机休眠  1~5秒
                randomSleep();
            }
        }
    }

    /**
     * 解析全部的列表文件内容 并解析出相关的详情页面存入本地文件
     * 每个list页面有128条数据详情。。
     *
     * @throws Exception
     */
    private static void parseAllListFile() throws Exception {
        File _58PathFile = get58File();
        JSONObject jsonObject = new JSONObject();
        for (File file : _58PathFile.listFiles()) {
            if (StringUtils.startsWith(file.getName(), FILE_NAME_58_LIST_HTML)) {
                String html = FileUtils.readFileToString(file, CommonConstants.ENCODING);

                Document document = JsoupUtils.parse(html);

                String refer = document.select("link[rel=canonical]").attr("href");

                List<String> urlList = new ArrayList<>(0);

                // 128条子数据
                Elements list_li = document.select("[class='house-list-wrap'] li");
                for (Element every : list_li) {
                    Elements pic = every.select("[class=pic]").select("a");
                    String href = pic.attr("href");
                    urlList.add(href);
                }
                JSONArray jsonArray = new JSONArray();
                jsonArray.addAll(urlList);
                jsonObject.put(refer, jsonArray);
            }
        }
        writeJsonToFile(jsonObject);
    }


    private static void parseAllInfo() throws Exception {

        File _58JsonFile = get58File();
        JSONObject jsonObject = readFileToJson();

        Set<Map.Entry<String, Object>> set = jsonObject.entrySet();
        for (Map.Entry<String, Object> entry : set) {
            // 从哪个列表来的
            String key = entry.getKey();
            // 该列表关联的详情url
            Object value = entry.getValue();
            if (JSONArray.class.isAssignableFrom(value.getClass())) {
                JSONArray jsonArray = (JSONArray) value;
                Iterator iterator = jsonArray.iterator();
                while (iterator.hasNext()) {
                    // https://sy.58.com/ershoufang/38840683082017x.shtml?utm_source=&spm=
                    String infoUrl = iterator.next().toString();
                    System.out.println(infoUrl);
                    try {
                        Document document = JsoupUtils.connect(infoUrl).referrer(key).get();
                        String html = document.html();

                        String fileName = infoUrl.substring(infoUrl.lastIndexOf("/") + 1, infoUrl.lastIndexOf("."));

                        File file = new File(_58JsonFile.getPath() + File.separator + FILE_NAME_58_INFO_HTML + fileName + FILE_NAME_SUFFIX);
                        FileUtils.touch(file);
                        FileUtils.writeStringToFile(file, html, CommonConstants.ENCODING);
                        randomSleep(2, 4);
                    } catch (Exception e) {
                        log.warning(e.getMessage());
                        continue;
                    }
                }
            }
        }
    }


    /**
     * 解析全部详情内容
     *
     * @throws Exception
     */
    private static List<HouseInfo58> parseAll58Info() throws Exception {
        List<HouseInfo58> voList = new ArrayList<>(0);
        File _58Path = get58File();
        File[] files = _58Path.listFiles((o1, o2) -> StringUtils.startsWith(o2, FILE_NAME_58_INFO_HTML));
        Arrays.stream(files)
//                .parallel()
                .forEach(o -> {
                    try {
                        String html = FileUtils.readFileToString(o, CommonConstants.ENCODING);
                        HouseInfo58 vo = parse58Info(html);
                        System.out.println(vo);
                        voList.add(vo);
                    } catch (Exception e) {
                        log.warning(e.getMessage());
                    }
                });
//        for (File file : files) {
//            String html = FileUtils.readFileToString(file, CommonConstants.ENCODING);
//            HouseInfo58 vo = parse58Info(html);
//            voList.add(vo);
//        }
        return voList;
    }


    public static void main1(String[] args) throws Exception {
        log.info("任务开始执行时间" + new DateTime().toString("yyyy-MM-dd HH:mm:ss.SSS"));

        ExecutorService service = Executors.newFixedThreadPool(3);

        List<Callable<List<String[]>>> tasks = new ArrayList<>(0);

        List<String> pnList = new ArrayList<>(0);
        // 总共页数看数据是有70页  模拟造71页时没有数据显示了，最晚显示的是7-31的数据
        int totalPage = 70;
        for (int i = 1; i < totalPage; i++) {
            String pnUrl = "https://sy.58.com/ershoufang/0/pn" + i + "/?ClickID=1";
            pnList.add(pnUrl);
        }
        // 每7页一组任务
        for (int i = 0; i < 7; i++) {
            List<String> subPnList = pnList.subList(7 * i, 7 * (i + 1));
            tasks.add(executeTask(subPnList));
        }

        List<Future<List<String[]>>> results = service.invokeAll(tasks);

        List<String[]> dataList = new ArrayList<>(0);

        for (Future<List<String[]>> future : results) {
            dataList.addAll(future.get());
        }

        service.shutdown();

        String[] title58 = new String[]{"详情页url", "标题", "meta描述", "总价"};

        XSSFWorkbook workbook = ExcelUtil.make2007Excel("58", title58, dataList);

        File file = new File("D:/ershoufang.xls");
        FileUtils.touch(file);
        FileOutputStream output = FileUtils.openOutputStream(file);
        workbook.write(output);
        output.flush();
        log.info("任务结束执行时间" + new DateTime().toString("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    /**
     * 每组多少页
     *
     * @param pnList
     * @return
     */
    private static Callable<List<String[]>> executeTask(List<String> pnList) {
        final List<String> taskUrl = Lists.newArrayList(pnList);
        Callable<List<String[]>> task = new Callable<List<String[]>>() {
            @Override
            public List call() throws Exception {
                List<String[]> everyList = new ArrayList<>(0);
                for (String pnUrl : taskUrl) {
                    // 每页的条数 获取列表的详情集合url
                    List<String> list58 = get58ListUrl(pnUrl);
                    int index = 0;
                    for (String url : list58) {
//                        if (index == 50) {
//                            break;
//                        }
                        log.info(url);
                        if (urlCOWSet.add(url)) {
                            HouseInfo58 model = get58HouseInfo(url);
                            everyList.add(new String[]{
                                    model.getInfoUrl(),
                                    model.getTitle(),
                                    model.getMetaDescription(),
                                    model.getTotalPrice()
                            });
                        } else {
                            log.warning("url路径已爬取过：" + url);
                        }
                        index++;
                        Long sleepTime = CommonConstants.localRandom.nextLong(1000L, 5000L);
                        Thread.sleep(sleepTime);
                    }
                    Long sleepTime = CommonConstants.localRandom.nextLong(1000L, 5000L);
                    Thread.sleep(sleepTime);
                }


                return everyList;
            }
        };
        return task;
    }

    /**
     * 获取58列表数据 每个详情页url
     *
     * @return
     * @throws Exception
     */
    private static List<String> get58ListUrl(String pnUrl) throws Exception {
        List<String> urlList = new ArrayList<>(0);
        Document document = JsoupUtils.connect(pnUrl).get();
        Elements list_li = document.select("[class='house-list-wrap'] li");
        for (Element every : list_li) {
            Elements pic = every.select("[class=pic]").select("a");
            String href = pic.attr("href");
//            System.out.println(href);
            urlList.add(href);
        }
        return urlList;
    }

    /**
     * 获取58文件夹的路径
     *
     * @return
     */
    private static File get58File() {
        String path = Application.class.getClassLoader().getResource("58").getFile();
        return new File(path);
    }

    private static void get58ListUrl2JsonFile(String pageUrl) throws Exception {
        List<String> urlList = new ArrayList<>(0);
        Connection connection = JsoupUtils.connect(pageUrl);
        Document document = connection.get();
        Elements list_li = document.select("[class='house-list-wrap'] li");
        for (Element every : list_li) {
            Elements pic = every.select("[class=pic]").select("a");
            String href = pic.attr("href");
            urlList.add(href);
        }

        JSONArray jsonArray = new JSONArray();
        jsonArray.addAll(urlList);
        File file = get58File();
        String infoJson = FileUtils.readFileToString(file, CommonConstants.ENCODING);
        JSONArray array = JSON.parseArray(infoJson);
        array.iterator();
    }

    /**
     * 根据html详情页面解析成vo内容
     *
     * @param html
     * @return
     */
    private static HouseInfo58 parse58Info(String html) {
        Document document = JsoupUtils.parse(html);
        System.out.println("######head title########################");
        String headTitle = document.select("html head title").text();
        System.out.println(headTitle);
        String canonical = document.select("html head link[rel=canonical]").attr("href");
        System.out.println(canonical);
        System.out.println("######meta content############");
        String content = document.select("html head meta[name='description']").attr("content");
        System.out.println(content);

        Optional<String> count = Stream.of(content.split(";")[1].split("；")).filter(o -> StringUtils.startsWith(o, "售价：")).findFirst();
        String totalPrice = count.get();
        System.out.println(totalPrice);

        String perSquare = totalPrice.substring(totalPrice.indexOf("（") + 1, totalPrice.lastIndexOf("元"));
        System.out.println(perSquare);
        System.out.println("######title ############");
        String title = document.select("div[class=house-title] h1[class=c_333 f20]").text();
        System.out.println(title);
        System.out.println("#######房子结构信息 generalSituation###########");
        String room = document.select("div[id=generalSituation] ul[class=general-item-left] li:eq(1) span[class=c_000]").text();
        String area = document.select("div[id=generalSituation] ul[class=general-item-left] li:eq(2) span[class=c_000]").text();
        String toward = document.select("div[id=generalSituation] ul[class=general-item-left] li:eq(3) span[class=c_000]").text();

        String floor = document.select("div[id=generalSituation] ul[class=general-item-right] li:eq(0) span[class=c_000]").text();
        String decoration = document.select("div[id=generalSituation] ul[class=general-item-right] li:eq(1) span[class=c_000]").text();
        String propertyRight = document.select("div[id=generalSituation] ul[class=general-item-right] li:eq(2) span[class=c_000]").text();
        System.out.println(room);
        System.out.println(area);
        System.out.println(toward);
        System.out.println(floor);
        System.out.println(decoration);
        System.out.println(propertyRight);

        String community = document.select("ul[class=house-basic-item3] li:eq(0) span:eq(1)").text();
        String region = document.select("ul[class=house-basic-item3] li:eq(1) span:eq(1)").text();

        System.out.println("##########概述信息 generalDesc############");
        String generalDesc = document.select("div[id=generalDesc] div[class=genaral-pic-desc]:eq(0) p[class=pic-desc-word]").text();
        String houseNum = document.select("div[id=generalDesc] div[class=genaral-pic-desc]:eq(1) p[class=pic-desc-word]").text();
        System.out.println(generalDesc);
        System.out.println(houseNum);
        System.out.println("#######联系方式############");
        String phoneNum = document.select("[class=phone-num]").text();
        System.out.println(phoneNum);
        System.out.println("########首付参考##############");
        String downPayment = document.select("div[id=generalExpense] ul[class=general-item-right] li span[class=c_000]").text();
        System.out.println(downPayment);
        System.out.println("########（可能）发布日期##iso 8601格式 没带时区 可能默认是北京时间+08:00###############");
        String ldjson = document.select("script[type=application/ld+json]").html();
        JSONObject jsonObject = JSON.parseObject(ldjson);
        String pubDate = jsonObject.getString("pubDate");
        System.out.println(pubDate);

        System.out.println("########小图url地址##############");
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
                .imgUrlList(imgUrlList)
                .build();
        return model;
    }


    /**
     * 详情地址
     * ps ：因为58是字体库加密，随着网页的打开会加载一个自定义的fangchan-secret 用base64加密的
     * 类似这种
     * <style>@font-face{font-family:'fangchan-secret';src:url('data:application/font-ttf;charset=utf-8;base64,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') format('truetype')}.strongbox{font-family:'fangchan-secret','Hiragino Sans GB','Microsoft yahei',Arial,sans-serif,'宋体'!important}</style>")}else{d.write("<style>@font-face{font-family:'fangchan-secret';src:url('https://testv1.wos.58dns.org/rtSOnMlKjrtUz/test/34290b8c1f804df5c2a682aa3564ce29.eot')}.strongbox{font-family:'fangchan-secret','Hiragino Sans GB','Microsoft yahei',Arial,sans-serif,'宋体'!important}.strongbox{visibility:hidden}</style>");var i=d.createElement('img');i.onerror=function(){setTimeout(function(){var s=document.createElement('style'),n=document.getElementsByTagName('script')[0];s.type='text/css';s.styleSheet.cssText='.strongbox{visibility:visible!important}';n.parentNode.insertBefore(s,n);},1300)};i.src='https://testv1.wos.58dns.org/rtSOnMlKjrtUz/test/34290b8c1f804df5c2a682aa3564ce29.eot';}}(window,document);
     * 类似解密的网上搜索有很多 反正大概思路就是把base64下载到本地字体 并计算偏移量内容 进行关联映射最后得到实际的数字，
     * 但在仔细观察seo的meta标签时发现有总价信息。
     * 另：jsoup 的selector模式可以参考博客https://www.cnblogs.com/yueshutong/p/9381530.html
     *
     * @param infoUrl
     * @return
     * @throws Exception
     */
    private static HouseInfo58 get58HouseInfo(String infoUrl) throws Exception {
        Document document = JsoupUtils.connect(infoUrl).get();
        String html = document.html();
//        System.out.println(html);
        System.out.println("######head title########################");
        String headTitle = document.select("html head title").text();
        System.out.println(headTitle);
        String canonical = document.select("html head link[rel=canonical]").attr("href");
        System.out.println(canonical);
        System.out.println("######meta content############");
        String content = document.select("html head meta[name='description']").attr("content");
        System.out.println(content);

        Optional<String> count = Stream.of(content.split(";")[1].split("；")).filter(o -> StringUtils.startsWith(o, "售价：")).findFirst();
        String cost = count.get();
        System.out.println(cost);
        System.out.println("######title ############");
        String title = document.select("div[class=house-title] h1[class=c_333 f20]").text();
        System.out.println(title);
        System.out.println("#######房子结构信息 generalSituation###########");
        String room = document.select("div[id=generalSituation] ul[class=general-item-left] li:eq(1) span[class=c_000]").text();
        String area = document.select("div[id=generalSituation] ul[class=general-item-left] li:eq(2) span[class=c_000]").text();
        String toward = document.select("div[id=generalSituation] ul[class=general-item-left] li:eq(3) span[class=c_000]").text();
        System.out.println(room);
        System.out.println(area);
        System.out.println(toward);

        System.out.println("#######房子结构信息 house_basic_item2###########");
        Elements house_basic_item2 = document.select("[class=house-basic-item2]");
        String room2 = house_basic_item2.select("p[class=room]").text();
        String area2 = house_basic_item2.select("p[class=area]").text();
        String toward2 = house_basic_item2.select("p[class=toward]").text();
        System.out.println(room2);
        System.out.println(area2);
        System.out.println(toward2);
        System.out.println("##########概述信息 generalDesc############");
        String generalDesc = document.select("div[id=generalDesc] div[class=genaral-pic-desc]:eq(0) p[class=pic-desc-word]").text();
        String houseNum = document.select("div[id=generalDesc] div[class=genaral-pic-desc]:eq(1) p[class=pic-desc-word]").text();
        System.out.println(generalDesc);
        System.out.println(houseNum);
        System.out.println("#######联系方式############");
        String phoneNum = document.select("[class=phone-num]").text();
        System.out.println(phoneNum);
        System.out.println("########首付参考##############");
        String downPayment = document.select("div[id=generalExpense] ul[class=general-item-right] li span[class=c_000]").text();
        System.out.println(downPayment);
        System.out.println("########（可能）发布日期##iso 8601格式 没带时区 可能默认是北京时间+08:00###############");
        String ldjson = document.select("script[type=application/ld+json]").html();
//        System.out.println(ldjson);
        JSONObject jsonObject = JSON.parseObject(ldjson);
        String pubDate = jsonObject.getString("pubDate");
        System.out.println(pubDate);


        HouseInfo58 model = HouseInfo58.builder()
                .infoUrl(infoUrl)
                .headTitle(headTitle)
                .metaDescription(content)
                .metaCanonical(canonical)
                .pubDate(pubDate)
                .title(title)
                .downPayment(downPayment)
                .totalPrice(cost)
                .room(room)
                .area(area)
                .toward(toward)
                .floor("")
                .decoration("")
                .propertyRight("")


                .totalPrice(cost)
                .build();
        return model;
    }


    /**
     * 1~5秒直接随机睡眠
     *
     * @throws Exception
     */
    private static void randomSleep() throws Exception {
        randomSleep(1, 5);
    }

    /**
     * start~end秒之间睡眠
     *
     * @param start
     * @param end
     * @throws Exception
     */
    private static void randomSleep(int start, int end) throws Exception {
        Long sleepTime = CommonConstants.localRandom.nextLong(start * 1000L, end * 1000L);
        Thread.sleep(sleepTime);
    }

    /**
     * 写入json file内容
     *
     * @param jsonObject
     * @throws Exception
     */
    private static void writeJsonToFile(JSONObject jsonObject) throws Exception {
        File _58PathFile = get58File();
        File file = new File(_58PathFile.getPath() + File.separator + FILE_NAME_58_LIST_JSON);
        FileUtils.touch(file);
        FileUtils.writeStringToFile(file, jsonObject.toJSONString(), CommonConstants.ENCODING);
    }

    /**
     * 从json file读取内容
     *
     * @return
     * @throws Exception
     */
    private static JSONObject readFileToJson() throws Exception {
        File _58PathFile = get58File();
        File file = new File(_58PathFile.getPath() + File.separator + FILE_NAME_58_LIST_JSON);
        String json = FileUtils.readFileToString(file, CommonConstants.ENCODING);
        return JSON.parseObject(json);
    }
}

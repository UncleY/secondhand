# secondhand


爬取相关网站房源（二手房）信息
如：58（个人房源）、芒果、链家等。

用WebMagic抓取房源数据信息 将爬取的html内容存储至本地
然后用Jsoup分析获取相关元素内容 组成vo或excel进行页面输出

###反爬虫
芒果是js动态跳转（正常访问的页面数据都很慢10秒+） 无法使用正常的Jsoup/httpclient进行获取html内容
采取selenium + phantomJs 模拟浏览器进行数据爬取
但获取联系人api没做限制

58有防爬措施及验证码，可采用proxy ip代理或降低每分爬取频率
58列表页带置顶的数据链接 是有重定向的 要获取到最终的实际页面
另外58页面有字体加密font secret随机变化的 不过查看源代码发现seo的description中有实际房价信息
如有感兴趣解密加密字体可以参考这些文章
https://www.cnblogs.com/a595452248/p/10800845.html
https://www.jianshu.com/p/a5d904c5d88e

###相关文档

**webmagic**项目地址 https://github.com/code4craft/webmagic 中文文档 http://webmagic.io/

**jsoup**项目地址 https://github.com/jhy/jsoup/ 官网 https://jsoup.org/

**selenium**项目地址 https://github.com/SeleniumHQ/selenium  中文文档 https://seleniumhq.github.io/docs/site/zh-cn/
ps:最新版的selenium不再支持phantomjs

**PhantomJS**项目地址 https://github.com/ariya/phantomjs  目前停止更新版本 替代的话可以找Firefox Headless及Headless Chrome都可以


###bug list
2019-8-11  
1）按每页N条数据*M页的总数，可能会有漏爬几个详情页，暂未找到原因。  

2）win10 下webDriver池里的phantom.exe总关闭不了（占用内存高）  



###其他信息
jdk 1.8.0.181  win7/10  idea2017.2.6




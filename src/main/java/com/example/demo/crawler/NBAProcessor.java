package com.example.demo.crawler;

import com.example.demo.entity.NewPage;
import com.example.demo.utils.ESutil;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by song on 2018/3/9.
 */
//虎扑nba
public class NBAProcessor implements PageProcessor {
    // 抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);
    private static int count =0;


    public Site getSite() {
        return site;
    }


    public void process(Page page) {

        if(!page.getUrl().regex("https://voice.hupu.com/nba/\\d+.html").match()){
            //加入满足条件的链接
            page.addTargetRequests(page.getHtml().xpath("//div[@class=\"list-hd\"]/h4/a/@href").all());

            List<String> all = page.getHtml().xpath("//div[@class=\"voice-paging\"]/a/@href").all();
            List<String> collect = all.stream().map(s -> "https://voice.hupu.com" + s).collect(Collectors.toList());
            page.addTargetRequests(collect);
        }else{
            //获取页面需要的内容
            System.out.println("headline："+ page.getHtml().xpath("//h1[@class=\"headline\"]/text()").get());
            System.out.println("content"+page.getHtml().xpath("//div[@class=\"artical-main-content\"]/allText()").get());
            System.out.println("time"+page.getHtml().xpath("//span[@id=\"pubtime_baidu\"]/text()").get());
            System.out.println("url"+page.getUrl().toString());

            String headline=page.getHtml().xpath("//h1[@class=\"headline\"]/text()").get();
            String content=page.getHtml().xpath("//div[@class=\"artical-main-content\"]/allText()").get();
            String time=page.getHtml().xpath("//span[@id=\"pubtime_baidu\"]/text()").get();
            String url=page.getUrl().toString();
            NewPage newPage=new NewPage(headline,content,time,url);
            add(newPage);


            count ++;
        }
    }

    public static void main(String[] args) {
        long startTime, endTime;
        System.out.println("开始...");
        startTime = System.currentTimeMillis();

        Spider.create(new NBAProcessor()).addUrl("https://voice.hupu.com/nba/1").thread(5).run();

        endTime = System.currentTimeMillis();
        System.out.println("爬取结束，耗时约" + ((endTime - startTime) / 1000) + "秒，抓取了"+count+"条记录");
    }

    public synchronized void  add(NewPage newPage){
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("headline", newPage.getHeadline())
                    .field("content", newPage.getContent())
                    .field("time", newPage.getTime())
                    .field("url", newPage.getUrl())
                    .endObject();

            IndexResponse response = ESutil.client()
                    .prepareIndex("nba", "news")
                    .setSource(builder)
                    .get();


        } catch (IOException e) {
            e.printStackTrace();

        }


    }

}

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
 * Created by song on 2018/3/12.
 */
public class QQNBAProcessor implements PageProcessor {
    // 抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);
    private static int count =0;


    public Site getSite() {
        return site;
    }


    public void process(Page page) {
        if(!page.getUrl().regex("http://sports.qq.com/a/\\d+/\\d+.htm").match()){
            //加入满足条件的链接
            List<String> all = page.getHtml().xpath("//a[@class=\"fs14\"]/@href").all();
            System.out.println(all);
            List<String> collect = all.stream().map(s -> "http://sports.qq.com" + s).collect(Collectors.toList());
            page.addTargetRequests(collect);
        }else{
            //获取页面需要的内容
            System.out.println("headline："+ page.getHtml().xpath("//div[@class=\"hd\"]/h1/text()").get());
            System.out.println("content"+page.getHtml().xpath("//div[@class=\"Cnt-Main-Article-QQ\"]/allText()").get());
            System.out.println("time"+page.getHtml().xpath("//span[@class=\"a_time\"]/text()").get());
            System.out.println("url"+page.getUrl().toString());

            String headline=page.getHtml().xpath("//div[@class=\"hd\"]/h1/text()").get();
            String content=page.getHtml().xpath("//div[@class=\"Cnt-Main-Article-QQ\"]/allText()").get();
            String time=page.getHtml().xpath("//span[@class=\"a_time\"]/text()").get();
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

        Spider.create(new QQNBAProcessor())
                .addUrl("http://sports.qq.com/basket/nba/morenews.htm").thread(5).run();

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

            IndexResponse response = ESutil.client().prepareIndex("nbanews", "news")
                    .setSource(builder)
                    .get();


        } catch (IOException e) {
            e.printStackTrace();

        }


    }

}

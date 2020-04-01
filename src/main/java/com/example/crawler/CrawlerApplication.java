package com.example.crawler;

import com.example.crawler.pageProcessor.BugPageProcessor;
import com.example.crawler.pageProcessor.PatchPageProcessor;
import com.example.crawler.pipeline.BugPipeline;
import com.example.crawler.pipeline.PatchPipeline;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;

@SpringBootApplication
@MapperScan("com.example.crawler.mapper")
@EnableScheduling
public class CrawlerApplication implements CommandLineRunner {

    @Autowired
    private BugPageProcessor bugPageProcessor;

    @Autowired
    private PatchPageProcessor patchPageProcessor;
    @Autowired
    private BugPipeline bugPipeline;

    @Autowired
    private PatchPipeline patchPipeline;

    public static void main(String[] args) {
        SpringApplication.run(CrawlerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        /*Spider.create(bugPageProcessor)
                .addUrl("http://www.cnnvd.org.cn/web/vulnerability/querylist.tag?pageno=1&repairLd=")
                //.addUrl("https://www.zhihu.com/explore")
                .addPipeline(bugPipeline)
                .thread(4)
                .run();

        Spider.create(patchPageProcessor)
                .addUrl("http://www.cnnvd.org.cn/web/cnnvdpatch/querylist.tag?pageno=1")
                .addPipeline(patchPipeline)
                .thread(4)
                .run();

         */
    }
}

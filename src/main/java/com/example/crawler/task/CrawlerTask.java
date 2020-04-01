package com.example.crawler.task;

import com.example.crawler.pageProcessor.BugPageProcessor;
import com.example.crawler.pageProcessor.PatchPageProcessor;
import com.example.crawler.pipeline.BugPipeline;
import com.example.crawler.pipeline.PatchPipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;

@Component
public class CrawlerTask {

    @Autowired
    private BugPageProcessor bugPageProcessor;

    @Autowired
    private PatchPageProcessor patchPageProcessor;
    @Autowired
    private BugPipeline bugPipeline;

    @Autowired
    private PatchPipeline patchPipeline;


    @Scheduled(cron = "0 0 0 * * *")
    public void scheduled() {
        Spider.create(bugPageProcessor)
                .addUrl("http://www.cnnvd.org.cn/web/vulnerability/querylist.tag?pageno=1&repairLd=")
                .addPipeline(bugPipeline)
                .thread(4)
                .run();

        Spider.create(patchPageProcessor)
                .addUrl("http://www.cnnvd.org.cn/web/cnnvdpatch/querylist.tag?pageno=1")
                .addPipeline(patchPipeline)
                .thread(4)
                .run();
    }
}

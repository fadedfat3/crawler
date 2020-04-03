package com.example.crawler.pageProcessor;

import com.example.crawler.model.BugModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@Configurable
public class BugPageProcessor  implements PageProcessor {
    private Site site = Site.me().setRetryTimes(3).setSleepTime(3000).setTimeOut(600000);
    @Value("${crawler.everyday-page}")
    private int MAX_PAGE;
    private AtomicInteger pageno = new AtomicInteger(1);
    private AtomicInteger retryCount = new AtomicInteger(1);
    private final String URL_LIST = "http://www\\.cnnvd\\.org\\.cn/web/vulnerability/querylist\\.tag\\?pageno=\\d+&repairLd=";
    private final String URL_BUG = "http://www\\.cnnvd\\.org\\.cn/web/xxk/ldxqById\\.tag\\?CNNVD=CNNVD-\\d+-\\d+";

    @Override
    public void process(Page page) {
        if (page.getUrl().regex(URL_LIST).match() && pageno.get() <= MAX_PAGE){
            ArrayList<String> urls = new ArrayList<>(page.getHtml().links().regex(URL_BUG).all());
            if (!urls.isEmpty()) {
                page.addTargetRequests(urls);
                page.addTargetRequest("http://www.cnnvd.org.cn/web/vulnerability/querylist.tag?pageno=" + pageno.incrementAndGet() + "&repairLd=");
            }else {
                page.addTargetRequest(page.getUrl().toString().split("#")[0] + "#" + retryCount.getAndIncrement());
            }
            page.setSkip(true);

        }else if (page.getUrl().regex(URL_BUG).match()){
            BugModel bugModel = new BugModel();
            String name = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/h2/text()");
            bugModel.setName(name);

            String cnnvd = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[1]/span/text()");
            if(cnnvd != "") {
                String[] strings =  cnnvd.split("-");
                cnnvd = "CNNVD-" + strings[1] + "-" + strings[2];
            }
            bugModel.setCnnvd(cnnvd);

            String degree = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[2]/a/text()");
            bugModel.setDegree(degree);

            String cve = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[3]/a/text()");
            bugModel.setCve(cve);

            String type = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[4]/a/text()");
            bugModel.setType(type);

            String createdTime = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[5]/a/text()");
            bugModel.setCreatedTime(createdTime);

            String threatType = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[6]/a/text()");
            bugModel.setThreatType(threatType);

            String updatedTime = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[7]/a/text()");
            bugModel.setUpdatedTime(updatedTime);

            String vendor = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[8]/a/text()");
            bugModel.setVendor(vendor);

            String source = getElementByXpath(page, "//*[@id=\"1\"]/a/text()");
            bugModel.setSource(source);

            String description = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[3]/p[1]/text()") +
                    getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[3]/p[2]/text()");
            bugModel.setDescription(description);

            String notice = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[4]/p[1]/text()");
            String url = getElementByXpath(page, "//*[@class='ldgg']/text()");
            bugModel.setNotice(notice + url);

            String website = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[5]/p[1]/text()") +
                    getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[5]/p[2]/text()") +
                    getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[5]/p[2]/a/text()");
            bugModel.setWebsite(website);

            String entity = getElementByXpath(page, "//*[@id=\"ent\"]/p/text()");
            bugModel.setEntity(entity);
            if (bugModel.getCve().isEmpty()) {
                String failedUrl = page.getUrl().toString();
                int count = 0;
                if (failedUrl.split("#").length > 1) {
                    count = Integer.parseInt(failedUrl.split("#")[1]);
                    if (count < 10) {
                        page.addTargetRequest(page.getUrl().toString().split("#")[0] + "#" + (++count));
                    }else {
                        log.warn("Can not find bug:{}", failedUrl);
                    }
                }else {
                    page.addTargetRequest(page.getUrl().toString().split("#")[0] + "#" + (++count));
                }
                page.setSkip(true);
            }else {
                page.putField("bug", bugModel);
            }
        }else {
            page.setSkip(true);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    private String getElementByXpath(Page page, String xpath){
        String s = page.getHtml().xpath(xpath).toString();
        if (s == null) {
            return "";
        }else{
            return s.trim();
        }
    }

    public BugPageProcessor() {

    }

    public BugPageProcessor(int maxPage, int pageno) {
        this.MAX_PAGE = maxPage;
        this.pageno.set(pageno);
    }

    public int getPageno() {
        return pageno.get();
    }
}

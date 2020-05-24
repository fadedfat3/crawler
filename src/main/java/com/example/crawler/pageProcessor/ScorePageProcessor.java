package com.example.crawler.pageProcessor;

import com.example.crawler.mapper.BugMapper;
import com.example.crawler.model.BugModel;
import com.example.crawler.model.ScoreModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.Collections;

@Component
@Slf4j
public class ScorePageProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(3000).setTimeOut(600000);
    @Value("${crawler.once}")
    private boolean once;
    private String URL_MONTH = "https://nvd\\.nist\\.gov/vuln/full-listing/\\d+/\\d+";
    private String URL_FULL_LIST = "https://nvd\\.nist\\.gov/vuln/full-listing";
    private String URL_DETAIL = "https://nvd\\.nist\\.gov/vuln/detail/CVE-\\d+-\\d+";
    @Autowired
    private BugMapper bugMapper;
    @Override
    public void process(Page page) {
        if (page.getUrl().regex(URL_FULL_LIST).match()) {
            if (once) {
                ArrayList<String> urls = new ArrayList<>(page.getHtml().links().regex(URL_MONTH).all());
                if (!urls.isEmpty()) {
                    page.addTargetRequests(urls);
                }
            } else {
                ArrayList<String> months = new ArrayList<>(page.getHtml().xpath("//*[@id=\"page-content\"]/div/ul[1]/li").links().regex(URL_MONTH).all());
                if (!months.isEmpty()) {
                    Collections.sort(months);
                    page.addTargetRequest(months.get(months.size() - 1));
                    log.info("last month:{}", months.get(months.size() - 1));
                }
            }
            page.setSkip(true);
        }
        if (page.getUrl().regex(URL_MONTH).match()) {
            ArrayList<String> urls = new ArrayList<>(page.getHtml().links().regex(URL_DETAIL).all());
            if (!urls.isEmpty()) {
                for(String url : urls) {
                    String cve = url.substring(url.lastIndexOf("/")+1);
                    BugModel bugModel =  bugMapper.selectByCve(cve);
                    if (bugModel == null || bugModel.getScore() - 0 < 0.001) {
                        page.addTargetRequest(url);
                    }
                }
            }
            page.setSkip(true);
        }
        if (page.getUrl().regex(URL_DETAIL).match()) {
            String[] arrays = page.getUrl().toString().split("/");
            String cve = arrays[arrays.length - 1];
            String score = page.getHtml().xpath("//*[@id=\"p_lt_WebPartZone1_zoneCenter_pageplaceholder_p_lt_WebPartZone1_zoneCenter_VulnerabilityDetail_VulnFormView_Cvss3NistCalculatorAnchor\"]/text()").toString();
            if (score != null && !score.isEmpty()) {
                score = score.split(" ")[0];
                double scoreNum = Double.parseDouble(score);
                ScoreModel scoreModel = new ScoreModel();
                scoreModel.setCve(cve);
                scoreModel.setScore(scoreNum);
                page.putField("score",scoreModel);
            }
        }
    }

    @Override
    public Site getSite() {
        return site;
    }
}

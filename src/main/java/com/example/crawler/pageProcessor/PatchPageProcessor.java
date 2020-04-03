package com.example.crawler.pageProcessor;

import com.example.crawler.model.PatchModel;
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
@Configurable
@Slf4j
public class PatchPageProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(3000).setTimeOut(600000);
    @Value("${crawler.everyday-page}")
    private int MAX_PAGE;
    private AtomicInteger pageno = new AtomicInteger(1);
    private AtomicInteger retryCount = new AtomicInteger(1);
    private final String URL_LIST = "http://www\\.cnnvd\\.org\\.cn/web/cnnvdpatch/querylist\\.tag\\?pageno=\\d+";
    private final String URL_BUG = "http://www\\.cnnvd\\.org\\.cn/web/xxk/ldxqById\\.tag\\?CNNVD=CNNVD-\\d+-\\d+";
    private final String URL_PATCH = "http://www\\.cnnvd\\.org\\.cn/web/xxk/bdxqById\\.tag\\?id=\\d+";


    @Override
    public void process(Page page) {
        if (page.getUrl().regex(URL_LIST).match() && pageno.get() <= MAX_PAGE){
            ArrayList<String> urls = new ArrayList<>(page.getHtml().links().regex(URL_PATCH).all());
            if(!urls.isEmpty()){
                page.addTargetRequests(urls);
                page.addTargetRequest("http://www.cnnvd.org.cn/web/cnnvdpatch/querylist.tag?pageno="  + pageno.incrementAndGet());
            }else {
                page.addTargetRequest(page.getUrl().toString().split("#")[0]  + "#" + retryCount.getAndIncrement());
            }
            page.setSkip(true);

        }else if (page.getUrl().regex(URL_PATCH).match()){
            PatchModel patchModel = new PatchModel();
            String patchName = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/h2/text()");
            patchModel.setName(patchName);

            String patchCnpd = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[1]/text()");
            patchModel.setCnpd(patchCnpd);

            String size = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[2]/text()");
            patchModel.setSize(size);

            String patchDegree = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[3]/text()");
            patchModel.setDegree(patchDegree);

            String time = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[4]/text()");
            patchModel.setTime(time);

            String patchVendor = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[5]/text()");
            patchModel.setVendor(patchVendor);

            String vendorHomepage = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[6]/a/text()");
            patchModel.setVendorHomepage(vendorHomepage);

            String md5 = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[2]/ul/li[7]/text()");
            patchModel.setMd5(md5);

            String patchWebsite = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[3]/p/text()") +
                    getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[3]/p/a/text()");
            patchModel.setWebsite(patchWebsite);

            String patchNotice = getElementByXpath(page, "/html/body/div[4]/div/div[1]/div[4]/p/text()");
            patchModel.setNotice(patchNotice);

            ArrayList<String> bugUrls = new ArrayList<>(page.getHtml().links().regex(URL_BUG).all());

            if (bugUrls.size() > 0) {
                String cnnvdNo = bugUrls.get(0).split("=")[1];
                page.putField("cnnvd", cnnvdNo);
                page.putField("patch", patchModel);
            }else if (patchModel.getCnpd().isEmpty()){
                String failedUrl = page.getUrl().toString();
                int count = 0;
                if (failedUrl.split("#").length > 1) {
                    count = Integer.parseInt(failedUrl.split("#")[1]);
                    if (count < 10) {
                        page.addTargetRequest(page.getUrl().toString().split("#")[0] + "#" + (++count));
                    }else {
                        log.warn("Can not find patch:{}", failedUrl);
                    }
                }else {
                    page.addTargetRequest(page.getUrl().toString().split("#")[0] + "#" + (++count));
                }
                page.setSkip(true);
            }else {
                page.putField("patch", patchModel);
            }
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

    public PatchPageProcessor() {

    }

    public PatchPageProcessor(int maxPage, int pageno) {
        this.MAX_PAGE = maxPage;
        this.pageno.set(pageno);
    }

    public int getPageno() {
        return pageno.get();
    }
}

package com.example.crawler.pipeline;

import com.example.crawler.mapper.PatchMapper;
import com.example.crawler.model.BugPatchModel;
import com.example.crawler.model.PatchModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

@Slf4j
@Component
public class PatchPipeline implements Pipeline {
    @Autowired
    private PatchMapper patchMapper;

    @Override
    public void process(ResultItems resultItems, Task task) {
        PatchModel patchModel = resultItems.get("patch");
        String cnnvd = resultItems.get("cnnvd");
        if (patchModel != null && !patchModel.getCnpd().isEmpty()) {
            PatchModel old = patchMapper.selectByCnpd(patchModel.getCnpd());
            if (old == null) {
                log.info("新增补丁{}", patchModel);
                patchMapper.insert(patchModel);
            }
            if (cnnvd != null && !cnnvd.isEmpty()) {
                BugPatchModel bugPatchModel = patchMapper.select(cnnvd, patchModel.getCnpd());
                if (bugPatchModel == null) {
                    patchMapper.add(cnnvd, patchModel.getCnpd());
                }
            }
        }
    }
}

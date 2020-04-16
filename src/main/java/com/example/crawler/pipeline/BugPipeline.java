package com.example.crawler.pipeline;

import com.example.crawler.mapper.BugMapper;
import com.example.crawler.mapper.ScoreMapper;
import com.example.crawler.model.BugModel;
import com.example.crawler.model.ScoreModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

@Slf4j
@Component
public class BugPipeline implements Pipeline {

    @Autowired
    private BugMapper bugMapper;

    @Autowired
    private ScoreMapper scoreMapper;

    @Override
    public void process(ResultItems resultItems, Task task) {
        BugModel bugModel = resultItems.get("bug");
        if (bugModel == null || bugModel.getCve().isEmpty()) {
            return;
        }
        BugModel old = bugMapper.selectByCve(bugModel.getCve());
        ScoreModel scoreModel = scoreMapper.selectByCve(bugModel.getCve());
        if (scoreModel != null && scoreModel.getScore() - bugModel.getScore() > 0.000001) {
            bugModel.setScore(scoreModel.getScore());
        }
        if(old == null) {

            log.info("新增漏洞{}", bugModel);
            bugMapper.insert(bugModel);
        }
        else  {
            bugModel.setId(old.getId());
            if(!old.equals(bugModel)) {
                log.info("更新漏洞{}", bugModel);
                bugMapper.update(bugModel);
            }
        }

    }
}

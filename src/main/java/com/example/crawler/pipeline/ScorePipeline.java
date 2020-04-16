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

@Component
@Slf4j
public class ScorePipeline implements Pipeline {

    @Autowired
    private ScoreMapper scoreMapper;

    @Autowired
    private BugMapper bugMapper;

    @Override
    public void process(ResultItems resultItems, Task task) {
        ScoreModel scoreModel = resultItems.get("score");
        if (scoreModel != null && !scoreModel.getCve().isEmpty()) {
            ScoreModel old = scoreMapper.selectByCve(scoreModel.getCve());
            if (old == null) {
                log.info("新增score:{}", scoreModel);
                scoreMapper.insert(scoreModel);
            }
            BugModel bugModel = bugMapper.selectByCve(scoreModel.getCve());
            if (bugModel != null && bugModel.getScore() - 0 < 0.0000001) {
                bugModel.setScore(scoreModel.getScore());
                bugMapper.update(bugModel);
            }
        }
    }
}

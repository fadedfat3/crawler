package com.example.crawler.mapper;

import com.example.crawler.model.ScoreModel;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoreMapper {
    void insert(ScoreModel scoreModel);
    ScoreModel selectByCve(String cve);
}

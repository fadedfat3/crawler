package com.example.crawler.mapper;

import com.example.crawler.model.BugModel;
import org.springframework.stereotype.Repository;

@Repository
public interface BugMapper {
    void insert(BugModel bugModel);
    BugModel selectByCve(String cve);
    void update(BugModel bugModel);
    BugModel selectByCnnvd(String cnnvd);
}

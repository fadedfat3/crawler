package com.example.crawler.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Repository;

@Data
@EqualsAndHashCode
public class BugModel {
    private Integer id;
    private String name;
    private String degree;
    private double score;
    private String type;
    private String threatType;
    private String vendor;
    private String source;
    private String cve;
    private String cnnvd;
    private String description;
    private String notice;
    private String website;
    private String entity;
    private String createdTime;
    private String updatedTime;
    private String nvd;




}

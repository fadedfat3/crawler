package com.example.crawler.model;

import lombok.Data;

@Data
public class PatchModel {
    private Integer id;
    private String name;
    private String cnpd;
    private String size;
    private String degree;
    private String time;
    private String vendor;
    private String vendorHomepage;
    private String md5;
    private String website;
    private String notice;
}

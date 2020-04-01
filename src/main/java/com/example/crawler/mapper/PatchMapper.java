package com.example.crawler.mapper;

import com.example.crawler.model.BugPatchModel;
import com.example.crawler.model.PatchModel;
import org.springframework.stereotype.Repository;

@Repository
public interface PatchMapper {
    void insert(PatchModel patchModel);
    PatchModel selectByCnpd(String cnpd);
    void add(String cnnvd, String cnpd);
    BugPatchModel select(String cnnvd, String cnpd);
}

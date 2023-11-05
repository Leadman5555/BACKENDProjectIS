package com.project.projectmanagment_is.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

import static java.util.Arrays.stream;

@Getter
@AllArgsConstructor
public class ProjectDto {
    private Long creatorDevId;
    private String projectName;
    private String devIdList;

    public void setDevIdList(String devIdList) {
        this.devIdList = devIdList;
    }

    @JsonIgnore
    public List<Long> getDevIdList_Long() {
        String[] ids = devIdList.split(",");
        return stream(ids)
                .map(Long::parseLong)
                .toList();
    }
}

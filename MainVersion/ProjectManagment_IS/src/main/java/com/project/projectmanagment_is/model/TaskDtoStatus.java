package com.project.projectmanagment_is.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaskDtoStatus {
    private String taskComment;
    private Long assignedDevId;
    private String taskState;
    private String dateDone;
    private Long doneDevId;
}

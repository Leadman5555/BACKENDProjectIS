package com.project.projectmanagment_is.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
public class TaskDto {
    private Long creatorDevId;
    private String dateCreated;
    private String taskName;
    private int estimation;
    private String specialization;
    private String taskComment;
    private Long assignedDevId;
}

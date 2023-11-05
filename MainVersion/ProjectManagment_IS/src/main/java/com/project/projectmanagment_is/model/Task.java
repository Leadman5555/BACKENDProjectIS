package com.project.projectmanagment_is.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "task")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taskId")
    private Long taskId;
    @Column(name = "projectId")
    private Long projectId;
    @Column(name = "creatorDevId")
    private Long creatorDevId;
    @Column(name = "dateCreated")
    private LocalDate dateCreated;
    @Column(name = "taskName")
    private String taskName;
    @Column(name = "estimation")
    private int estimation;
    @Column(name = "specialization")
    @Enumerated(EnumType.STRING)
    private Specialization specialization;
    @Column(name = "taskComment")
    private String taskComment;
    @Column(name = "assignedDevId")
    private Long assignedDevId;
    @Column(name = "taskState")
    private boolean taskState; //True is completed, false is not completed
    @Column(name = "dateDone")
    private LocalDate dateDone;//Data when the task was declared as completed
    @Column(name = "timeDone")
    private Long timeDone; //Number of days between the creation and competition of the task
    @Column(name = "doneDevId")
    private Long doneDevId;//Id of the developer who completed the task

}

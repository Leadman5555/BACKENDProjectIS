package com.project.projectmanagment_is.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static java.util.Arrays.stream;

@Entity
@Getter
@Table(name = "project")
@AllArgsConstructor
@NoArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "projectId")
    private Long projectId;
    @Column(name = "creatorDevId")
    private Long creatorDevId;
    @Column(name = "projectName")
    private String projectName;
    @Column(name = "devIdList")
    private String devIdList;
    @Column(name = "taskIdList")
    private String taskIdList;


    @JsonIgnore
    public List<Long> getDevIdList_Long() {
        String[] ids = devIdList.split(",");
        return stream(ids)
                .map(Long::parseLong)
                .toList();
    }

    @JsonIgnore
    public List<Long> getTaskIdList_Long() {
        if (!taskIdList.isEmpty()) {
            String[] ids = taskIdList.split(",");
            return stream(ids)
                    .map(Long::parseLong)
                    .toList();
        }
        return null;
    }

    //adds task id as string to the task list
    public void addTaskToList(Long id) {
        taskIdList += (id + ",");
    }

    @Override
    public String toString() {
        String output = "Project{" + "\n Project Id: " + projectId + "\n Project creator Id: " + creatorDevId
                + "\n Project name: " + projectName + "\n Ids of developers involved in the project: " + devIdList;
        if (taskIdList == "null")//Due to MySQL null String misinterpretation
            output += "\n Ids of tasks added to the project: " + taskIdList;
        else
            output += "\n No tasks added to the project.";
        return output + "\n}";
    }
}

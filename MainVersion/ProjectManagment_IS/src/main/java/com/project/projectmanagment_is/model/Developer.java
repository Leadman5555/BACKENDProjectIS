package com.project.projectmanagment_is.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static java.util.Arrays.stream;

@Entity
@Table(name = "developer")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Developer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "devId")
    private Long devId;
    @Column(name = "name")
    private String name;
    @Column(name = "projectIds")
    private String projectIds; //Split by ',': "1,2,3,"
    @Column(name = "specialization")
    @Enumerated(EnumType.STRING)
    private Specialization specialization;
    @Column(name = "doneTaskIds")
    private String doneTaskIds;// Split by ',': "1,2,3,"


    //Adds the project Id to the String
    public void addProjectToList(Long id) {
        projectIds += (id + ",");
    }

    //Adds the important parts of completed task data as a String in format:
    //"doneTaskId-doneTaskEstimation-doneTaskTimeDone,"
    public void addTaskToList(Long id, int estimation, Long timeDone) {
        doneTaskIds += (id + "-" + estimation + "-" + timeDone + ",");
    }

    @JsonIgnore
    public List<Long> getProjectIds_Long() {
        if (!projectIds.isEmpty()) {
            String[] ids = projectIds.split(",");
            return stream(ids)
                    .map(Long::parseLong)
                    .toList();
        }
        return null;
    }

    public void setProjectIds(String projectIds) {
        this.projectIds = projectIds;
    }
}

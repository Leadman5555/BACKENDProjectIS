package com.project.projectmanagment_is;

import lombok.Getter;
import lombok.Setter;

//Custom pair model for the list
@Getter
@Setter
public class Pair<L, R> {
    private L taskId;
    private R devId;

    //This pair model is used for the convenient display by ResponseEntity body
    public Pair(L taskId, R devId) {
        this.taskId = taskId;
        this.devId = devId;
    }

}

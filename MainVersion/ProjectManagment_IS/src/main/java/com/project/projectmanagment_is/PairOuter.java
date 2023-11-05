package com.project.projectmanagment_is;

import lombok.Getter;
import lombok.Setter;

//Universal pair model
@Getter
@Setter
public class PairOuter<L, R> {
    private L l;
    private R r;

    public PairOuter(L l, R r) {
        this.l = l;
        this.r = r;
    }

    @Override
    public String toString() {
        return "ProjectId:" + l +
                " Assignment pairs:" + r;
    }
}

package com.garbagesorting.android;

import com.garbagesorting.android.db.Garbage;

public class Result {

    private String name;

    private int label;

    private float prob;

    public Result(String name, int label, float prob) {
        this.name = name;
        this.label = label;
        this.prob = prob;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public float getProb() {
        return prob;
    }

    public void setProb(float prob) {
        this.prob = prob;
    }
}

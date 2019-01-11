package com.cmcglobal.plugins.dto;

import com.cmcglobal.plugins.entity.TestCaseType;
import com.cmcglobal.plugins.utils.Helper;

public class TestCaseProductivity {
    private long volume;
    private double time;
    private TestCaseType type;

    public TestCaseProductivity(long volume, double time, TestCaseType type) {
        this.volume = volume;
        this.time = time;
        this.type = type;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public TestCaseType getType() {
        return type;
    }

    public void setType(TestCaseType type) {
        this.type = type;
    }

    public double getProductivity(TestCaseType function) {
        double exchange = (double) this.type.getPerformance()/function.getPerformance();
        return this.volume/exchange/this.time*8;
    }
}

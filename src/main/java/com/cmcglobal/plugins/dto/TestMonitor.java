package com.cmcglobal.plugins.dto;

public class TestMonitor {
    private int quantity;
    private double time;
    private Object productivity = "";
    private boolean enoughQuantity = false;
    private boolean enoughProductivity = false;

    public TestMonitor() {
    }

    public TestMonitor(int quantity, double time, Object productivity) {
        this.quantity = quantity;
        this.time = time;
        this.productivity = productivity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public Object getProductivity() {
        return productivity;
    }

    public void setProductivity(Object productivity) {
        this.productivity = productivity;
    }

    public boolean isEnoughQuantity() {
        return enoughQuantity;
    }

    public void setEnoughQuantity(boolean enoughQuantity) {
        this.enoughQuantity = enoughQuantity;
    }

    public boolean isEnoughProductivity() {
        return enoughProductivity;
    }

    public void setEnoughProductivity(boolean enoughProductivity) {
        this.enoughProductivity = enoughProductivity;
    }
}

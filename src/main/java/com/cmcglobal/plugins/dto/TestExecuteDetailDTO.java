package com.cmcglobal.plugins.dto;

import java.util.List;

public class TestExecuteDetailDTO {
    private       int          no;
    private       String       testCaseType;
    private       List<String> definition;
    private       long         value;
    private       double       conversionRate;
    private       long         numberOfTestCases;
    private       double       time;
    private final Double       productivity;

    public TestExecuteDetailDTO(final int no, final String testCaseType, final List<String> definition,
                                final long value, final double conversionRate, final long numberOfTestCases,
                                final double time) {
        this.no = no;
        this.testCaseType = testCaseType;
        this.definition = definition;
        this.value = value;
        this.conversionRate = conversionRate;
        this.numberOfTestCases = numberOfTestCases;
        this.time = time;
        productivity = (conversionRate == 0 || time == 0) ? null : numberOfTestCases / conversionRate / time * 8;
    }

    public int getNo() {
        return no;
    }

    public void setNo(final int no) {
        this.no = no;
    }

    public String getTestCaseType() {
        return testCaseType;
    }

    public void setTestCaseType(final String testCaseType) {
        this.testCaseType = testCaseType;
    }

    public List<String> getDefinition() {
        return definition;
    }

    public void setDefinition(final List<String> definition) {
        this.definition = definition;
    }

    public long getValue() {
        return value;
    }

    public void setValue(final long value) {
        this.value = value;
    }

    public double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(final double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public long getNumberOfTestCases() {
        return numberOfTestCases;
    }

    public void setNumberOfTestCases(final long numberOfTestCases) {
        this.numberOfTestCases = numberOfTestCases;
    }

    public double getTime() {
        return time;
    }

    public void setTime(final double time) {
        this.time = time;
    }

    public Double getProductivity() {
        return productivity;
    }
}

package com.zt;

import java.math.BigDecimal;

public class TextInfo {
    private String text;
    private BigDecimal textCount;
    private BigDecimal textProportion;
    private double second;

    public TextInfo() {
    }

    public TextInfo(String text, BigDecimal textCount, double second) {
        this.text = text;
        this.textCount = textCount;
        this.second = second;
    }

    public TextInfo(String text, BigDecimal textCount, BigDecimal textProportion) {
        this.text = text;
        this.textCount = textCount;
        this.textProportion = textProportion;
    }

    public TextInfo(String text, BigDecimal textCount, BigDecimal textProportion, double second) {
        this.text = text;
        this.textCount = textCount;
        this.textProportion = textProportion;
        this.second = second;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public BigDecimal getTextCount() {
        return textCount;
    }

    public void setTextCount(BigDecimal textCount) {
        this.textCount = textCount;
    }

    public BigDecimal getTextProportion() {
        return textProportion;
    }

    public void setTextProportion(BigDecimal textProportion) {
        this.textProportion = textProportion;
    }

    public double getSecond() {
        return second;
    }

    public void setSecond(double second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "TextInfo{" +
                "text='" + text + '\'' +
                ", textCount=" + textCount +
                '}';
    }
}

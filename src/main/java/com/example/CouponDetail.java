package com.example;

public class CouponDetail {

    private final String code;
    private final String dueDate;

    public CouponDetail(String code, String dueDate) {
        this.code = code;
        this.dueDate = dueDate;
    }

    public String getCode() {
        return code;
    }

    public String getDueDate() {
        return dueDate;
    }
}

package com.template.contracts;

public class Recital {
    final private Integer recitalIndex;
    final private String recital;
    final private String recitalDetails;

    public Recital(Integer recitalIndex, String recital, String recitalDetails) {
        this.recitalIndex = recitalIndex;
        this.recital = recital;
        this.recitalDetails = recitalDetails;
    }

    public static Recital createWithNoDetails(Integer recitalIndex, String recital) {
        return new Recital(recitalIndex, recital, "");
    }
}
package com.ebe.miniaelec.domain.model;

import java.io.Serializable;

public class CardEMVData implements Serializable {
    private String AID;
    private String ApplicationLabel;
    private String CVMRes;
    private String TSI;
    private String TVR;

    public CardEMVData() {
    }

    public CardEMVData(String AID, String applicationLabel, String CVMRes, String TSI, String TVR) {
        this.AID = AID;
        this.ApplicationLabel = applicationLabel;
        this.CVMRes = CVMRes;
        this.TSI = TSI;
        this.TVR = TVR;
    }

    public String getAID() {
        return AID;
    }

    public void setAID(String AID) {
        this.AID = AID;
    }

    public String getApplicationLabel() {
        return ApplicationLabel;
    }

    public void setApplicationLabel(String applicationLabel) {
        this.ApplicationLabel = applicationLabel;
    }

    public String getCVMRes() {
        return CVMRes;
    }

    public void setCVMRes(String CVMRes) {
        this.CVMRes = CVMRes;
    }

    public String getTSI() {
        return TSI;
    }

    public void setTSI(String TSI) {
        this.TSI = TSI;
    }

    public String getTVR() {
        return TVR;
    }

    public void setTVR(String TVR) {
        this.TVR = TVR;
    }
}

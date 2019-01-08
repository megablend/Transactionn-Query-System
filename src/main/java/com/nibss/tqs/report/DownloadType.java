package com.nibss.tqs.report;

/**
 * Created by Emor on 8/1/2016.
 */
public enum DownloadType {
    CSV("text/csv"),
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PDF("application/pdf");

    private String mimeType;
     DownloadType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
}

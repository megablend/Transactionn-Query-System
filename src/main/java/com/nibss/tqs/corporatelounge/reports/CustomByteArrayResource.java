package com.nibss.tqs.corporatelounge.reports;

import org.springframework.core.io.ByteArrayResource;

public class CustomByteArrayResource extends ByteArrayResource {

    private String description;

    public CustomByteArrayResource(byte[] byteArray) {
        super(byteArray);
        description = "";
    }

    public CustomByteArrayResource(byte[] byteArray, String description) {
        super(byteArray, description);
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }
}

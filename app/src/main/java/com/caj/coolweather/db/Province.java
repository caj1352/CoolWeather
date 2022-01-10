package com.caj.coolweather.db;

import org.litepal.crud.LitePalSupport;

public class Province extends LitePalSupport {
    private String provinceName;

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }
}

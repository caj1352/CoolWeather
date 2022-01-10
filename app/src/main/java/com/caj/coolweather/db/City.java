package com.caj.coolweather.db;

import org.litepal.crud.LitePalSupport;

public class City extends LitePalSupport {
    private String cityName;
    private String provinceName;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }
}

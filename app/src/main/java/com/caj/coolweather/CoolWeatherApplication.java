package com.caj.coolweather;

import android.app.Application;

import org.litepal.LitePal;

public class CoolWeatherApplication extends Application {

    private static CoolWeatherApplication INSTANCE;

    private String token = ""; // 和风天气token

    private static final String devBaseUrl = "https://devapi.qweather.com"; // 开发版

    private static final String apiBaseUrl = "https://api.qweather.com"; // 商业版

    private String baseUrl = devBaseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getToken() {
        return token;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        LitePal.initialize(this);
    }

    public static CoolWeatherApplication getInstance() {
        return INSTANCE;
    }
}

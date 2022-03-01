package com.caj1352.coolweather;

import android.app.Application;

import org.litepal.LitePal;

public class CoolWeatherApplication extends Application {

    private static CoolWeatherApplication INSTANCE;

    private String token = ""; // 和风天气token

    private String baseUrl = "https://devapi.qweather.com";

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

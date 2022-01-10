package com.caj.coolweather;

import android.app.Application;

import org.litepal.LitePal;

public class CoolWeatherApplication extends Application {

    private static CoolWeatherApplication INSTANCE = null;

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

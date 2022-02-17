package com.caj.coolweather;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.widgets.analyzer.VerticalWidgetRun;
import androidx.core.view.GravityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.caj.coolweather.databinding.ActivityWeatherBinding;
import com.caj.coolweather.gson.Forecast;
import com.caj.coolweather.gson.Weather;
import com.caj.coolweather.util.HFUtil;
import com.caj.coolweather.util.HttpUtil;
import com.caj.coolweather.util.Utility;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public ActivityWeatherBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityWeatherBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        // 初始化各控件
        String weatherId = getIntent().getStringExtra("weather_id");
        viewBinding.weatherLayout.setVisibility(View.INVISIBLE);
        viewBinding.swipeRefresh.setColorSchemeResources(R.color.design_default_color_primary);
        viewBinding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        viewBinding.title.navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewBinding.drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        loadBingPic();
        requestWeather(weatherId);
    }

    /**
     * 根据天气id请求城市天气信息
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        // 如果Token没值就用假数据的接口
        if (TextUtils.isEmpty(CoolWeatherApplication.getInstance().getToken())) {
            Toast.makeText(this, "请在App配置授权码", Toast.LENGTH_SHORT).show();
        } else {
            // 如果Token有值就用真数据的接口
            requestHFWeather(weatherId);
        }
    }

    /**
     * 处理并展示Weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.more.info;
        viewBinding.title.titleCity.setText(cityName);
        viewBinding.title.titleUpdateTime.setText(updateTime);
        viewBinding.now.degreeText.setText(degree);
        viewBinding.now.weatherInfoText.setText(weatherInfo);
        viewBinding.forecast.forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item
                    , viewBinding.forecast.forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            viewBinding.forecast.forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            viewBinding.aqi.aqiText.setText(weather.aqi.city.aqi);
            viewBinding.aqi.pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运行指数：" + weather.suggestion.sport.info;
        viewBinding.suggestion.comfortText.setText(comfort);
        viewBinding.suggestion.carWashText.setText(carWash);
        viewBinding.suggestion.sportText.setText(sport);
        viewBinding.weatherLayout.setVisibility(View.VISIBLE);
        viewBinding.swipeRefresh.setRefreshing(false);
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestDingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendHttpRequest(requestDingPic, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String bingPic = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(viewBinding.bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 请求
     * @param weatherId
     */
    private void requestHFWeather(final String weatherId) {
        String location = weatherId.replace("CN", "");
        String baseUrl = CoolWeatherApplication.getInstance().getBaseUrl();
        String weatherUrl = baseUrl + "/v7/weather/now?location="
                + location + "&key=" + CoolWeatherApplication.getInstance().getToken();


        HttpUtil.sendHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气失败"
                                , Toast.LENGTH_SHORT).show();
                        viewBinding.swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseText = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject hfJsonObject = new JSONObject();
                            JSONArray hfJsonArray = new JSONArray();
                            hfJsonArray.put(new JSONObject());
                            hfJsonObject.put("HeWeather", hfJsonArray);
                            JSONObject jsonObject = new JSONObject(responseText);
                            if ("200".equals(jsonObject.getString("code"))) {
                                JSONObject weather = hfJsonArray.getJSONObject(0);
                                weather.put("status", "ok");

                                // 填入basic信息
                                JSONObject basic = new JSONObject();
                                basic.put("city", getIntent().getStringExtra("city"));
                                basic.put("id", weatherId);
                                JSONObject update = new JSONObject();
                                String updateTime = HFUtil.formatDateString(jsonObject
                                        .getString("updateTime"));
                                update.put("loc", updateTime);
                                basic.put("update", update);
                                weather.put("basic", basic);

                                // 填入now信息
                                JSONObject now = new JSONObject();
                                JSONObject fromNow = jsonObject.getJSONObject("now");
                                now.put("tmp", fromNow.getString("temp"));
                                JSONObject cond = new JSONObject();
                                cond.put("txt", fromNow.getString("text"));
                                now.put("cond", cond);
                                weather.put("now", now);
                                requestHFWeatherAQI(location, hfJsonObject.toString());
                            } else {
                                Toast.makeText(WeatherActivity.this, "获取天气失败"
                                        , Toast.LENGTH_SHORT).show();
                                viewBinding.swipeRefresh.setRefreshing(false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(WeatherActivity.this, "获取天气失败"
                                    , Toast.LENGTH_SHORT).show();
                            viewBinding.swipeRefresh.setRefreshing(false);
                        }



                    }
                });
            }
        });
    }

    private void requestHFWeatherAQI(final String location, String json) {
        String baseUrl = CoolWeatherApplication.getInstance().getBaseUrl();
        String weatherUrl = baseUrl + "/v7/air/now?location="
                + location + "&key=" + CoolWeatherApplication.getInstance().getToken();

        HttpUtil.sendHttpRequest(weatherUrl, new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseText = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject hfJsonObject = new JSONObject(json);
                            JSONArray hfJsonArray = hfJsonObject.getJSONArray("HeWeather");
                            JSONObject jsonObject = new JSONObject(responseText);
                            if ("200".equals(jsonObject.getString("code"))) {
                                JSONObject weather = hfJsonArray.getJSONObject(0);

                                // 填入AQI信息
                                JSONObject aqi = new JSONObject();
                                JSONObject now = jsonObject.getJSONObject("now");
                                JSONObject city = new JSONObject();
                                city.put("aqi", now.getString("aqi"));
                                city.put("pm25", now.getString("pm2p5"));
                                aqi.put("city", city);
                                weather.put("aqi", aqi);
                                requestHFWeatherSuggestion(location, hfJsonObject.toString());
                            } else {
                                Toast.makeText(WeatherActivity.this, "获取天气失败"
                                        , Toast.LENGTH_SHORT).show();
                                viewBinding.swipeRefresh.setRefreshing(false);
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(WeatherActivity.this, "获取天气失败"
                                    , Toast.LENGTH_SHORT).show();
                            viewBinding.swipeRefresh.setRefreshing(false);
                        }
                    }
                });

            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(WeatherActivity.this, "获取天气失败"
                        , Toast.LENGTH_SHORT).show();
                viewBinding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void requestHFWeatherSuggestion(final String location, String json) {
        String baseUrl = CoolWeatherApplication.getInstance().getBaseUrl();
        String weatherUrl = baseUrl + "/v7/indices/1d?location="
                + location + "&type=8,2,1"+"&key=" + CoolWeatherApplication.getInstance().getToken();

        HttpUtil.sendHttpRequest(weatherUrl, new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseText = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject hfJsonObject = new JSONObject(json);
                            JSONArray hfJsonArray = hfJsonObject.getJSONArray("HeWeather");
                            JSONObject jsonObject = new JSONObject(responseText);
                            if ("200".equals(jsonObject.getString("code"))) {
                                JSONObject weather = hfJsonArray.getJSONObject(0);

                                // 填入Suggestion信息
                                JSONObject suggestion = new JSONObject();
                                // 舒适指数
                                JSONObject fromComfort = jsonObject.getJSONArray("daily")
                                        .getJSONObject(0);
                                JSONObject comf = new JSONObject();
                                comf.put("txt", fromComfort.getString("text"));
                                suggestion.put("comf", comf);
                                // 洗车指数
                                JSONObject fromCarWash = jsonObject.getJSONArray("daily")
                                        .getJSONObject(1);
                                JSONObject cw = new JSONObject();
                                cw.put("txt", fromCarWash.getString("text"));
                                suggestion.put("cw", cw);
                                weather.put("suggestion", suggestion);
                                // 运动指数
                                JSONObject fromSport = jsonObject.getJSONArray("daily")
                                        .getJSONObject(2);
                                JSONObject sport = new JSONObject();
                                sport.put("txt", fromSport.getString("text"));
                                suggestion.put("sport", sport);
                                weather.put("suggestion", suggestion);
                                requestHFWeatherForecast(location, hfJsonObject.toString());
                            } else {
                                Toast.makeText(WeatherActivity.this, "获取天气失败"
                                        , Toast.LENGTH_SHORT).show();
                                viewBinding.swipeRefresh.setRefreshing(false);
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(WeatherActivity.this, "获取天气失败"
                                    , Toast.LENGTH_SHORT).show();
                            viewBinding.swipeRefresh.setRefreshing(false);
                        }
                    }
                });

            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(WeatherActivity.this, "获取天气失败"
                        , Toast.LENGTH_SHORT).show();
                viewBinding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void requestHFWeatherForecast(final String location, String json) {
        String baseUrl = CoolWeatherApplication.getInstance().getBaseUrl();
        String weatherUrl = baseUrl + "/v7/weather/3d?location="
                + location +"&key=" +  CoolWeatherApplication.getInstance().getToken();

        HttpUtil.sendHttpRequest(weatherUrl, new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String responseText = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            JSONObject hfJsonObject = new JSONObject(json);
                            JSONArray hfJsonArray = hfJsonObject.getJSONArray("HeWeather");
                            JSONObject jsonObject = new JSONObject(responseText);
                            if ("200".equals(jsonObject.getString("code"))) {
                                JSONObject weatherSrc = hfJsonArray.getJSONObject(0);

                                // 填入Forecast信息
                                JSONArray forecasts = new JSONArray();
                                JSONArray fromForecasts = jsonObject.getJSONArray("daily");
                                for (int i = 0; i < fromForecasts.length(); i++) {
                                    JSONObject forecast = new JSONObject();
                                    JSONObject fromForecast = fromForecasts.getJSONObject(i);
                                    forecast.put("date", fromForecast.getString("fxDate"));
                                    JSONObject tmp = new JSONObject();
                                    tmp.put("max", fromForecast.getString("tempMax"));
                                    tmp.put("min", fromForecast.getString("tempMin"));
                                    forecast.put("tmp", tmp);
                                    JSONObject cond = new JSONObject();
                                    cond.put("txt_d", fromForecast.get("textDay"));
                                    forecast.put("cond", cond);
                                    forecasts.put(forecast);
                                }
                                weatherSrc.put("daily_forecast", forecasts);
                                Weather weather = Utility.handleWeatherResponse(hfJsonObject.toString());
                                showWeatherInfo(weather);
                            } else {
                                Toast.makeText(WeatherActivity.this, "获取天气失败"
                                        , Toast.LENGTH_SHORT).show();
                                viewBinding.swipeRefresh.setRefreshing(false);
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(WeatherActivity.this, "获取天气失败"
                                    , Toast.LENGTH_SHORT).show();
                            viewBinding.swipeRefresh.setRefreshing(false);
                        }
                    }
                });

            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(WeatherActivity.this, "获取天气失败"
                        , Toast.LENGTH_SHORT).show();
                viewBinding.swipeRefresh.setRefreshing(false);
            }
        });
    }
}
package com.caj.coolweather.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.caj.coolweather.CoolWeatherApplication;
import com.caj.coolweather.MainActivity;
import com.caj.coolweather.WeatherActivity;
import com.caj.coolweather.adapter.ChooseAreaAdapter;
import com.caj.coolweather.databinding.ChooseAreaBinding;
import com.caj.coolweather.db.City;
import com.caj.coolweather.db.County;
import com.caj.coolweather.db.Province;
import com.caj.coolweather.util.HttpUtil;
import com.caj.coolweather.util.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    /**
     * 省级
     */
    private int LEVEL_PROVINCE = 1;

    /**
     * 市级
     */
    private int LEVEL_CITY = 2;

    /**
     * 县级
     */
    private int LEVEL_COUNTRY = 3;

    private ProgressDialog progressDialog;

    /**
     * 所在的省
     */
    private Province selectedProvince;

    /**
     * 所在的市
     */
    private City selectedCity;

    /**
     * 所有省
     */
    private List<Province> provinceList;

    /**
     * 所在省的所有市
     */
    private List<City> cityList;

    /**
     * 所在市的所有县
     */
    private List<County> countyList;

    private ChooseAreaAdapter adapter;
    private List<String> listData = new ArrayList<>();

    /**
     * 当前等级，用去表示显示的列表是省，市还是县
     */
    private int currentLevel = LEVEL_PROVINCE;

    ChooseAreaBinding viewBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding = ChooseAreaBinding.inflate(inflater, container, false);
        adapter = new ChooseAreaAdapter(listData);
        viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        viewBinding.recyclerView.setAdapter(adapter);
        viewBinding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext()
                , DividerItemDecoration.VERTICAL));
        return viewBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 给RecyclerView设置行点击事件。
        adapter.setOnItemClickListener(new ChooseAreaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {

                // 显示返回按钮
                viewBinding.backButton.setVisibility(View.VISIBLE);

                if (currentLevel == LEVEL_PROVINCE) { // 如果在省
                    selectedProvince = provinceList.get(position);
                    // 加载所在省的所有市
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) { // 如果在市
                    selectedCity = cityList.get(position);
                    // 加载所在市的所有镇
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTRY) { // 如果在镇
                    // 如果Token没值就用假数据的接口
                    if (TextUtils.isEmpty(CoolWeatherApplication.getInstance().getToken())) {
                        Toast.makeText(getContext(), "请在App配置授权码", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        String weatherId = countyList.get(position).getWeatherId();
                        String city = countyList.get(position).getCountyName();
                        // 保存weatherId 和 city
                        getActivity().getSharedPreferences("data", Context.MODE_PRIVATE)
                                .edit()
                                .putString("weatherId", weatherId)
                                .putString("city", city)
                                .apply();
                        if (getActivity() instanceof MainActivity) {
                            Intent intent = new Intent(getActivity(), WeatherActivity.class);
                            intent.putExtra("weather_id", weatherId);
                            intent.putExtra("city", city);
                            startActivity(intent);
                            getActivity().finish();
                        } else if (getActivity() instanceof WeatherActivity) {
                            WeatherActivity activity = (WeatherActivity) getActivity();
                            activity.viewBinding.swipeRefresh.setRefreshing(true);
                            activity.requestWeather(weatherId);
                        }

                    }
                }
            }
        });

        // 给返回按钮设置点击事件
        viewBinding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_CITY) { // 如果在市
                    queryProvinces();
                } else if (currentLevel == LEVEL_COUNTRY) { // 如果在镇
                    queryCities();
                }
            }
        });

        // 刚打开界面的时候，标题是中国
        viewBinding.titleText.setText("中国");
        SharedPreferences pref = getActivity()
                .getSharedPreferences("data", Context.MODE_PRIVATE);
        String weatherId = pref.getString("weatherId", null);
        String city = pref.getString("city", null);
        // 如果曾经打开过天气预报界面
        if (weatherId != null && getActivity() instanceof MainActivity) {
            // 直接打开上次打开过的天气预报界面
            Intent intent = new Intent(getActivity(), WeatherActivity.class);
            intent.putExtra("weather_id", weatherId);
            intent.putExtra("city", city);
            startActivity(intent);
            getActivity().finish();
        } else {
            // 刚打开界面的时候，RecyclerView显示的是省的列表
            queryProvinces();
        }


    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查查询到再去服务器上查询
     */
    void queryProvinces() {
        viewBinding.titleText.setText("中国");
        viewBinding.backButton.setVisibility(View.GONE);

        // 从数据库加载省列表
        provinceList = LitePal.findAll(Province.class);
        // 如果省列表有数据
        if (provinceList.size() > 0) {
            // 给RecyclerView重置成省列表
            listData.clear();
            for (Province province : provinceList) {
                listData.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_PROVINCE;
        } else {
            // 从网络获取省列表的数据
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    void queryCities() {
        viewBinding.titleText.setText(selectedProvince.getProvinceName());
        viewBinding.backButton.setVisibility(View.VISIBLE);

        // 从数据库加载市列表
        cityList = LitePal.where("provinceId = ?"
                , String.valueOf(selectedProvince.getId())).find(City.class);

        // 如果列市表有数据
        if (cityList.size() > 0) {
            // 给RecyclerView重置成市列表
            listData.clear();
            for (City city : cityList) {
                listData.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_CITY;
        } else {
            // 从网络获取市列表的数据
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查查询，如果没有查询到再去服务器上查询
     */
    void queryCounties() {
        viewBinding.titleText.setText(selectedCity.getCityName());
        viewBinding.backButton.setVisibility(View.VISIBLE);

        // 从数据库加载县列表
        countyList = LitePal.where("cityId = ?"
                , String.valueOf(selectedCity.getId())).find(County.class);

        // 如果县列表有数据
        if (countyList.size() > 0) {
            // 给RecyclerView重置成县列表
            listData.clear();
            for (County county : countyList) {
                listData.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            currentLevel = LEVEL_COUNTRY;
        } else {
            // 从Service获取县列表的数据
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型服务器上查询省市县数据
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountryResponse(responseText, selectedCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}

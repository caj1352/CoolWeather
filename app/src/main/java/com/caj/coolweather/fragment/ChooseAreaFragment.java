package com.caj.coolweather.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.caj.coolweather.adapter.ChooseAreaAdapter;
import com.caj.coolweather.databinding.ChooseAreaBinding;
import com.caj.coolweather.db.City;
import com.caj.coolweather.db.Country;
import com.caj.coolweather.db.Province;
import com.caj.coolweather.service.AreaService;
import com.caj.coolweather.util.Utility;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

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
     * 镇级
     */
    private int LEVEL_COUNTRY = 3;

    /**
     * 所在的省
     */
    private Province selectedProvince;

    /**
     * 所在的市
     */
    private City selectedCity;

    /**
     * 所在的镇
     */
    private Country selectedCountry;

    /**
     * 所有省
     */
    private List<Province> provinceList;

    /**
     * 所在省的所有市
     */
    private List<City> cityList;

    /**
     * 所在市的所有镇
     */
    private List<Country> countryList;

    private ChooseAreaAdapter adapter;
    private List<String> listData = new ArrayList<>();

    /**
     * 当前等级，用去表示显示的列表是，省，市还是镇
     */
    private int currentLevel = LEVEL_PROVINCE;

    ChooseAreaBinding viewBinding;

    AreaService areaService = AreaService.getInstance();

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
                    viewBinding.titleText.setText(selectedProvince.getProvinceName());
                    // 加载所在省的所有市
                    queryCities(selectedProvince.getProvinceName());
                    // 设置当前等级是市
                    currentLevel = LEVEL_CITY;
                } else if (currentLevel == LEVEL_CITY) { // 如果在市
                    selectedCity = cityList.get(position);
                    viewBinding.titleText.setText(selectedCity.getCityName());
                    // 加载所在市的所有镇
                    queryCounties(selectedCity.getProvinceName(), selectedCity.getCityName());
                    // 设置当前等级是镇
                    currentLevel = LEVEL_COUNTRY;
                } else if (currentLevel == LEVEL_COUNTRY) { // 如果在镇
                    selectedCountry = countryList.get(position);
                }
            }
        });

        // 给返回按钮设置点击事件
        viewBinding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_CITY) { // 如果在市
                    queryProvinces();
                    // 设置当前等级是省
                    currentLevel = LEVEL_PROVINCE;
                    viewBinding.titleText.setText("中国");
                    // 隐藏返回钮
                    viewBinding.backButton.setVisibility(View.GONE);
                } else if (currentLevel == LEVEL_COUNTRY) { // 如果在镇
                    queryCities(selectedProvince.getProvinceName());
                    // 设置当前等级是市
                    currentLevel = LEVEL_CITY;
                    viewBinding.titleText.setText(selectedProvince.getProvinceName());
                }
            }
        });

        // 刚打开界面的时候，标题是中国
        viewBinding.titleText.setText("中国");
        // 刚打开界面的时候，RecyclerView显示的是省的列表
        queryProvinces();

    }

    /**
     * 加载省列表
     */
    void queryProvinces() {
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
        } else {
            // 从Service获取省列表的数据
            areaService.getProvinces(new AreaService.CallBack() {
                @Override
                public void onSuccess(String response) {
                    // 保存到数据库
                    if (Utility.handleProvinceResponse(response)) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 重新调用本方法
                                queryProvinces();
                            }
                        });
                    }
                }

                @Override
                public void onFail(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 加载市列表
     * @param provinceName
     */
    void queryCities(String provinceName) {
        // 从数据库加载市列表
        cityList = LitePal.findAll(City.class);
        // 如果列市表有数据
        if (cityList.size() > 0) {
            // 给RecyclerView重置成市列表
            listData.clear();
            for (City city : cityList) {
                listData.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
        } else {
            // 从Service获取市列表的数据
            areaService.getCities(provinceName, new AreaService.CallBack() {
                @Override
                public void onSuccess(String response) {
                    // 保存到数据库
                    if (Utility.handleCityResponse(response)) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 重新调用本方法
                                queryCities(provinceName);
                            }
                        });
                    }
                }

                @Override
                public void onFail(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * 加载镇列表
     * @param provinceName
     * @param cityName
     */
    void queryCounties(String provinceName, String cityName) {
        // 从数据库加载镇列表
        countryList = LitePal.findAll(Country.class);
        // 如果镇列表有数据
        if (countryList.size() > 0) {
            // 给RecyclerView重置成市列表
            listData.clear();
            for (Country country : countryList) {
                listData.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
        } else {
            // 从Service获取镇列表的数据
            areaService.getCountries(provinceName, cityName, new AreaService.CallBack() {
                @Override
                public void onSuccess(String response) {
                    // 保存到数据库
                    if (Utility.handleCountryResponse(response)) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // 重新调用本方法
                                queryCounties(provinceName, cityName);
                            }
                        });
                    }
                }

                @Override
                public void onFail(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}

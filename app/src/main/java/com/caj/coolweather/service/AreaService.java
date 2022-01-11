package com.caj.coolweather.service;

import com.caj.coolweather.dao.AreaDao;

public class AreaService {
    AreaDao areaDao = AreaDao.getInstance();

    public interface CallBack {
        void onSuccess(String response);
        void onFail(Exception e);
    }

    private static AreaService INSTANCE = null;

    private AreaService(){}

    public static AreaService getInstance() {
        if (INSTANCE == null) {
            synchronized (AreaService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AreaService();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 从文件获取省数据
     * @param callBack
     */
    public void getProvinces(CallBack callBack) {
        areaDao.getProvinces(new AreaDao.CallBack() {
            @Override
            public void onSuccess(String response) {
                callBack.onSuccess(response);
            }

            @Override
            public void onFail(Exception e) {
                callBack.onFail(e);
            }
        });
    }

    /**
     * 获取市数据
     * @param provinceName
     * @param callBack
     */
    public void getCities(String provinceName, CallBack callBack) {
        areaDao.getCities(provinceName, new AreaDao.CallBack() {
            @Override
            public void onSuccess(String response) {
                callBack.onSuccess(response);
            }

            @Override
            public void onFail(Exception e) {
                callBack.onFail(e);
            }
        });
    }

    /**
     * 获取镇数据
     * @param provinceName
     * @param cityName
     * @param callBack
     */
    public void getCountries(String provinceName, String cityName, CallBack callBack) {
        areaDao.getCountries(provinceName, cityName, new AreaDao.CallBack() {
            @Override
            public void onSuccess(String response) {
                callBack.onSuccess(response);
            }

            @Override
            public void onFail(Exception e) {
                callBack.onFail(e);
            }
        });
    }
}

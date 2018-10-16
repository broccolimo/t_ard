package com.yzmc.service;

import com.yzmc.model.DeviceModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DeviceService {
    @GET("api/v1/order/getDeviceInfo")
    Call<DeviceModel> getDeviceInfo(
            @Query("order_id")
            String order_id
    );
}

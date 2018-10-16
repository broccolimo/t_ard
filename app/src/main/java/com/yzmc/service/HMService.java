package com.yzmc.service;

import com.yzmc.model.HMModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HMService {
    @GET("api/v1/order/maintaince0")
    Call<HMModel> getMaintainInfo(@Query("maintain_id")String maintain_id);
}

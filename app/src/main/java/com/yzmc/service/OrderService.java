package com.yzmc.service;

import com.yzmc.model.OrderArrayModel;
import com.yzmc.model.OrderModel;
import com.yzmc.model.XOrderArrayModel;

import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OrderService {

    //维保 加载工单池
    @GET("api/v1/order/showOrders")
    Call<OrderArrayModel> showOrders(
        @Query("account")
        String account
    );

    @GET("api/v1/order/getThisInfo")
    Call<OrderArrayModel> getThisInfo(
        @Query("account")
        String account
    );

    //由order_id获取order信息
    @GET("api/v1/order/Opt_2")
    Call<OrderModel> getOrderInfo(
        @Query("order_id")
        String order_id
    );

    //维修 加载工单池
    @GET("api/v1/order/showXOrders")
    Call<XOrderArrayModel> showXOrders(@Query("account")String account);

    //维修 加载我的任务
    @GET("api/v1/order/x_3")
    Call<XOrderArrayModel> showMyTaskX(@Query("account")String account);
}

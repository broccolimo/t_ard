package com.yzmc.service;

import com.yzmc.model.OperationModel;

import java.util.Date;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface OperationService {

    @POST("api/v1/order/Opt_1")
    @FormUrlEncoded
    Call<OperationModel> acceptBOrder(
        @Field("account")
        String account,
        @Field("name")
        String name,
        @Field("order_id")
        String order_id,
        @Field("accept_time")
        Date accept_time,
        @Field("company")
        String company
    );

    @POST("api/v1/order/Opt_3")
    @FormUrlEncoded
    Call<OperationModel> startBOrder(@Field("order_id")String order_id, @Field("time")Date time);

    @POST("api/v1/order/x_1")
    @FormUrlEncoded
    Call<OperationModel> acceptXOrder(@Field("xorder_id")String xorder_id, @Field("account")String account, @Field("name")String name, @Field("accept_time")Date accept_time);

    @POST("api/v1/order/x_11")
    @FormUrlEncoded
    Call<OperationModel> continueXOrder(@Field("account")String account, @Field("xorder_id")String xorder_id, @Field("accept_time")Date accept_time);
}

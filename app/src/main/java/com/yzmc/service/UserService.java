package com.yzmc.service;

import com.yzmc.model.UserModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserService {
    @FormUrlEncoded
    @POST("login")
    Call<UserModel> login(
            @Field("account")
            String account,
            @Field("pass")
            String pass
    );

    @GET("api/v1/user/getUserInfo")
    Call<UserModel> getUserInfo(
            @Query("account")
            String account
    );
}

package com.yzmc.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.SetCookie;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class v_list extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.v_list);
        AllActivity.addActivity(this);
    }

    @Override
    protected void onResume() {
        preload();
        super.onResume();
    }

    private void preload(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new SetCookie()).build();
                //Retrofit retrofit = new Retrofit.Builder().baseUrl(Constant.baseUrl)
            }
        }).start();
    }
}

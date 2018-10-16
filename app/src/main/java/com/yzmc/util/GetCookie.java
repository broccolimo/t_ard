package com.yzmc.util;

/**
 * @author jy
 * @created 2018-07-02
 * @description 获取cookie
 */

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Response;

public class GetCookie implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response fisrtResponse = chain.proceed(chain.request());
        String cookie = fisrtResponse.header("Set-Cookie");
        SharedPreferences.Editor config = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
        config.putString("cookie", cookie).commit();
        return fisrtResponse;
    }
}

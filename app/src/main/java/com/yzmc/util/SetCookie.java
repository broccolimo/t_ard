package com.yzmc.util;

/**
 * @author jy
 * @created 2018-07-02
 * @description 设置cookie
 */

import android.preference.PreferenceManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class SetCookie implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        String cookie = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).getString("cookie", "");
        builder.addHeader("Cookie", cookie);
        return chain.proceed(builder.build());
    }
}

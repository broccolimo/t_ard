package com.yzmc.util;

import android.content.Context;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Util_HttpConnect {

    public static String dealLogin(String url, String params, Context context){
        String result = "";
        try{
            URL _url = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(params.getBytes());
            outputStream.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            String line = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }
            Map<String, List<String>> cookiemap = conn.getHeaderFields();
            List<String> cookies = cookiemap.get("Set-Cookie");
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString("cookie", cookies.get(0)).commit();
            conn.disconnect();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static String getPostResult(String url, String params, Context context){
        String result = "";
        try{
            URL _url = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.addRequestProperty("Cookie", PreferenceManager.getDefaultSharedPreferences(context).getString("cookie", ""));

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(params.getBytes());
            outputStream.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            String line = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }
            conn.disconnect();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    public static String getGetResult(String url, Context context){
        String result = "";
        try{
            URL _url = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) _url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.addRequestProperty("Cookie", PreferenceManager.getDefaultSharedPreferences(context).getString("cookie", ""));
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            String line = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }
            conn.disconnect();

        }
        catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

}

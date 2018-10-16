package com.yzmc.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.yzmc.R;
import com.yzmc.model.UserModel;
import com.yzmc.service.UserService;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.GetCookie;
import com.yzmc.util.SetCookie;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Login extends AppCompatActivity{

    private EditText et_account;
    private EditText et_password;
    private CheckBox checkBox;
    private SharedPreferences.Editor editor;
    private SharedPreferences pref;
    private long currentTime;
    private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        AllActivity.addActivity(this);
        et_account = findViewById(R.id.et_account);
        et_password = findViewById(R.id.et_password);
        checkBox = findViewById(R.id.cb_remember);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRemember = pref.getBoolean("remember", false);
        if(isRemember){
            et_account.setText(pref.getString("account", ""));
            et_password.setText(pref.getString("pass", ""));
            checkBox.setChecked(true);
        }
    }

    //点击登录按钮
    public void login(View view){
        if(TextUtils.isEmpty(et_account.getText())){
            Toast.makeText(this, "请输入账户名", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(et_password.getText())){
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        showProgressDialog();
        currentTime = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .addInterceptor(new GetCookie())
                        .build();
                Retrofit retrofit = new Retrofit.Builder().baseUrl(Constant.baseUrl)
                        .addConverterFactory(GsonConverterFactory.create()).client(client).build();
                UserService userService = retrofit.create(UserService.class);
                Call<UserModel> call = userService.login(String.valueOf(et_account.getText()), String.valueOf(et_password.getText()));
                call.enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        int code = response.body().code;
                        if(code == 0){
                            if(checkBox.isChecked()){
                                editor = pref.edit();
                                editor.putBoolean("remember", true);
                                editor.putString("account", String.valueOf(et_account.getText()));
                                editor.putString("pass", String.valueOf(et_password.getText()));
                                editor.commit();
                            }
                            loadBaseInfo();
                        }
                        else{
                            Toast.makeText(Login.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        closeProgressDialog();
                        t.printStackTrace();
                        AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
                        dialog.setCancelable(false);
                        dialog.setTitle("提示");
                        if(System.currentTimeMillis() - currentTime > 10000){
                            dialog.setMessage("网络超时，请重新登录");
                        }
                        else{
                            dialog.setMessage("服务器维护中...");
                        }
                        dialog.setPositiveButton("确定", null);
                        dialog.show();
                    }
                });
            }
        }).start();


    }

    public void loadBaseInfo(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(new SetCookie())
                        .build();
                Retrofit retrofit = new Retrofit.Builder().baseUrl(Constant.baseUrl)
                        .addConverterFactory(GsonConverterFactory.create()).client(client).build();
                UserService userService = retrofit.create(UserService.class);
                Call<UserModel> call = userService.getUserInfo(pref.getString("account", ""));
                call.enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                        editor = pref.edit();
                        editor.putBoolean("canAccept_b", response.body().obj.canAccept_b);
                        editor.putBoolean("canAccept_x", response.body().obj.canAccept_x);
                        editor.putString("account", response.body().obj.account);
                        editor.putString("company", response.body().obj.company);
                        editor.putString("name", response.body().obj.name);
                        editor.putString("phone", response.body().obj.phone);
                        editor.putInt("role", response.body().obj.role);
                        editor.commit();
                        closeProgressDialog();
                        Intent intent = new Intent(Login.this, Main.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        t.printStackTrace();
                        AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
                        dialog.setCancelable(false);
                        dialog.setTitle("提示");
                        dialog.setMessage("网络超时，请重新登录");
                        dialog.setPositiveButton("确定", null);
                        dialog.show();
                    }
                });
            }
        }).start();
    }
    //连续点击2次返回键退出程序
    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() - currentTime < 2000){
            super.onBackPressed();
        }
        else{
            Toast.makeText(Login.this, "再按一次退出", Toast.LENGTH_SHORT).show();
            currentTime = System.currentTimeMillis();
        }
    }


    private ProgressDialog progressDialog;

    private void showProgressDialog(){
        progressDialog = new ProgressDialog(Login.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在登录...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

}

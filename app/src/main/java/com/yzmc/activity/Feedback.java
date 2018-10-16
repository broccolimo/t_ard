package com.yzmc.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.LinePathView;
import com.yzmc.util.StringCollection;
import com.yzmc.util.Util_HttpConnect;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Date;

public class Feedback extends AppCompatActivity {

    private LinePathView linePathView;
    private Button btn_reset;
    private Button btn_next;
    private RadioButton stf_0;
    private RadioButton stf_1;
    private RadioButton stf_2;
    private RadioButton stf_3;
    private ImageView back;


    private String order_id;


    private Context context = this;
    private SharedPreferences pref;
    private int satisfaction = -1;

    private boolean thread1IsOk = false;
    private boolean thread2IsOk = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);
        AllActivity.addActivity(this);
        Intent intent = getIntent();
        order_id = intent.getStringExtra("order_id");

        pref = PreferenceManager.getDefaultSharedPreferences(context);

        init();
        give();
    }

    private void init(){
        linePathView = findViewById(R.id.lpv);
        btn_reset = findViewById(R.id.btn_reset);
        btn_next = findViewById(R.id.btn_next);
        stf_0 = findViewById(R.id.stf_0);
        stf_1 = findViewById(R.id.stf_1);
        stf_2 = findViewById(R.id.stf_2);
        stf_3 = findViewById(R.id.stf_3);
        back = findViewById(R.id.back);
    }

    private void give(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linePathView.clear();
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(satisfaction == -1){
                    dialog_content("请选择用户满意度");
                    return;
                }
                if(!linePathView.getTouched()){
                    dialog_content("请用户签名");
                    return;
                }
                dialog_commit();
            }
        });

        stf_0.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                satisfaction = 0;
            }
        });

        stf_1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                satisfaction = 1;
            }
        });

        stf_2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                satisfaction = 2;
            }
        });

        stf_3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                satisfaction = 3;
            }
        });
    }

    private Handler mHandler = new Handler(){
        private int count = 0;
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == -1){
                dialog_over();
                return;
            }
            //thread1 签名照片本体
            if(msg.what == 9999){
                thread1IsOk = true;
            }
            //thread2 提交进程
            if(msg.what == 9998){
                thread2IsOk = true;
            }

            if(thread1IsOk && thread2IsOk){
                thread1IsOk = false;
                thread2IsOk = false;
                pref.edit().putBoolean("canAccept_b", true).commit();
                dialog_success();
                return;
            }
        }
    };

    private void dialog_over(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage(StringCollection.STR001);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Feedback.this, Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    private void dialog_content(String content){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage(content);
        dialog.setPositiveButton("确定", null);
        dialog.show();
    }

    private void dialog_commit(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage("提交后不可更改，确定要提交吗?");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bitmap bitmap = linePathView.getBitMap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] bitmapBytes = baos.toByteArray();
                final String base64 = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String url = Constant.baseUrl + "api/v1/order/sendSignature";
                        String params = "{\"base64\":\"" + base64 + "\",\"order_id\":\"" + order_id + "\"}";
                        String res = Util_HttpConnect.getPostResult(url, params, context);
                        try {
                            JSONObject json = new JSONObject(res);
                            int code = json.optInt("code");
                            Message msg = new Message();
                            if(code != 0){
                                msg.what = -1;
                                mHandler.sendMessage(msg);
                                return;
                            }
                            msg.what = 9999;
                            mHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            dialog_over();
                            return;
                        }
                    }
                }).start();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String url = Constant.baseUrl + "api/v1/order/Opt_7";
                        String params = "{\"account\":\"" + pref.getString("account", "") +
                                "\",\"order_id\":\"" + order_id +
                                "\",\"customer_satisfaction\":" + satisfaction +
                                ",\"customer_signature_path\":\"public/Signature/" + order_id + ".jpeg\"" +
                                ",\"time\":\"" + new Date() + "\"}";
                        String res = Util_HttpConnect.getPostResult(url, params, context);
                        try {
                            JSONObject json = new JSONObject(res);
                            int code = json.optInt("code");
                            Message msg = new Message();
                            if(code != 0){
                                msg.what = -1;
                                mHandler.sendMessage(msg);
                                return;
                            }
                            msg.what = 9998;
                            mHandler.sendMessage(msg);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            dialog_over();
                            return;
                        }
                    }
                }).start();
            }
        });
        dialog.setNegativeButton("我再想想", null);
        dialog.show();
    }


    private void dialog_success(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage("提交成功,请等待审核员审核");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Feedback.this, Main.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }
}

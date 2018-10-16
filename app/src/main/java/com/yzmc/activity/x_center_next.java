package com.yzmc.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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

public class x_center_next extends AppCompatActivity {

    private Button btn_next;
    private Button btn_prev;
    private Button btn_delay;
    private ImageView back;
    private LinePathView sign_official_signature;
    private Button btn_reset;
    private EditText et_customerOpinion;
    private EditText et_tel;

    private String xorder_id;
    private ProgressDialog progressDialog;
    private Context context = this;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_center_next);
        AllActivity.addActivity(this);
        init();
        give();
    }

    @Override
    protected void onResume() {
        new Task1(xorder_id).execute();
        super.onResume();
    }

    private void init(){
        btn_next = findViewById(R.id.btn_next);
        btn_prev = findViewById(R.id.btn_prev);
        btn_delay = findViewById(R.id.btn_delay);
        btn_reset = findViewById(R.id.btn_reset);
        back = findViewById(R.id.back);
        sign_official_signature = findViewById(R.id.sign_official_signature);
        et_customerOpinion = findViewById(R.id.et_customerOpinion);
        et_tel = findViewById(R.id.et_tel);

        xorder_id = getIntent().getStringExtra("xorder_id");
    }

    private void give(){
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sign_official_signature.getTouched()){
                    dialog_content("请客户单位签名");
                    return;
                }
                String customerOpinion = String.valueOf(et_customerOpinion.getText()).trim();
                if(customerOpinion.equals("")){
                    dialog_content("请填写用户意见");
                    return;
                }
                String tel = String.valueOf(et_tel.getText()).trim();
                if(tel.equals("")){
                    dialog_content("请填写客户单位联系电话");
                    return;
                }
                String regex = "^(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$";
                if(!tel.matches(regex)){
                    dialog_content("请输入正确的电话号码");
                    return;
                }
                Bitmap bitmap = sign_official_signature.getBitMap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] bitmapBytes = baos.toByteArray();
                String base64 = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP);
                showProgressDialog();
                new Task2(base64, xorder_id, customerOpinion, tel).execute();
            }
        });

        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sign_official_signature.clear();
            }
        });

        btn_delay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setCancelable(false);
                dialog.setTitle("提示");
                dialog.setMessage("确定要延时吗?提交后不可修改");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Task3(xorder_id).execute();
                    }
                });
                dialog.setNegativeButton("我再想想", null);
                dialog.show();
            }
        });
    }

    private class Task1 extends AsyncTask{
        private String xorder_id;
        private String Task1_Result;

        public Task1(String xorder_id){
            this.xorder_id = xorder_id;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/x_2?xorder_id=" + xorder_id;
            Task1_Result = Util_HttpConnect.getGetResult(url, context);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                JSONObject json = new JSONObject(Task1_Result);
                if(json.optInt("code") != 0){
                    closeProgressDialog();
                    dialog_over();
                    return;
                }
                String obj = json.optString("obj");
                JSONObject jobj = new JSONObject(obj);
                if(jobj.optString("customerOpinion") != "null"){
                    et_customerOpinion.setText(jobj.optString("customerOpinion"));
                }
                if(jobj.optString("tel") != "null"){
                    et_tel.setText(jobj.optString("tel"));
                }
                closeProgressDialog();
            } catch (Exception e) {
                e.printStackTrace();
                dialog_over();
                return;
            }
        }
    }

    private class Task2 extends AsyncTask{

        private String base64;
        private String xorder_id;
        private String customerOpinion;
        private String tel;

        private String r;

        public Task2(String base64, String xorder_id, String customerOpinion, String tel){
            this.base64 = base64;
            this.xorder_id = xorder_id;
            this.customerOpinion = customerOpinion;
            this.tel = tel;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/x_9";
            String params = "{\"base64\":\"" + base64 + "\",\"xorder_id\":\"" + xorder_id + "\",\"suffix\":\"_A\"" +
                    ",\"customerOpinion\":\"" + customerOpinion + "\",\"tel\":\"" + tel + "\",\"official_signature\":\"public/Signature/" + xorder_id + "_A.jpeg\",\"xorder_id\":\"" + xorder_id + "\"}";
            r = Util_HttpConnect.getPostResult(url, params, context);

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                JSONObject json = new JSONObject(r);
                if(json.optInt("code") != 0){
                    closeProgressDialog();
                    dialog_over();
                    return;
                }
                Intent intent = new Intent(x_center_next.this, x_center_end.class);
                Bundle bundle = new Bundle();
                bundle.putString("xorder_id", xorder_id);
                intent.putExtras(bundle);
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
                closeProgressDialog();
                dialog_over();
                return;
            }
        }
    }

    private class Task3 extends AsyncTask{
        private String xorder_id;
        private String r;

        public Task3(String xorder_id){
            this.xorder_id = xorder_id;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/x_5";
            String params = "{\"xorder_id\":\"" + xorder_id + "\",\"account\":\"" + PreferenceManager.getDefaultSharedPreferences(context).getString("account", "") + "\",\"delay_time\":\"" + new Date() + "\"}";
            r = Util_HttpConnect.getPostResult(url, params, context);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                JSONObject json = new JSONObject(r);
                if(json.optInt("code") != 0){
                    dialog_over();
                    return;
                }
                Intent intent = new Intent(x_center_next.this, Order_Success.class);
                Bundle bundle = new Bundle();
                bundle.putString("text", "延时成功");
                intent.putExtras(bundle);
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
                dialog_over();
                return;
            }

        }
    }

    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        progressDialog.setMessage("正在加载...");
        progressDialog.setCancelable(false);
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP
                        && keyCode == KeyEvent.KEYCODE_BACK
                        && event.getRepeatCount() == 0) {
                    dialog.dismiss();
                    onBackPressed();
                }
                return false;
            }
        });
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    private void dialog_over(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage(StringCollection.STR001);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(x_center_next.this, Login.class);
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

    private Handler mHandler = new Handler(){
        private int count = 0;
        @Override
        public void handleMessage(Message msg) {

        }
    };
}

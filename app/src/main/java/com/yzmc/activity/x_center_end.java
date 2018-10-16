package com.yzmc.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
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

public class x_center_end extends AppCompatActivity {


    private Context context = this;
    private String xorder_id;
    private ProgressDialog progressDialog;
    private ImageView back;
    private Button btn_delay;
    private LinePathView sign_director_signature;
    private Button btn_reset;
    private Button btn_prev;
    private Button btn_commit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_center_end);
        AllActivity.addActivity(this);
        init();
        give();
    }

    private void init(){
        back = findViewById(R.id.back);
        btn_delay = findViewById(R.id.btn_delay);
        sign_director_signature = findViewById(R.id.sign_director_signature);
        btn_reset = findViewById(R.id.btn_reset);
        btn_prev = findViewById(R.id.btn_prev);
        btn_commit = findViewById(R.id.btn_commit);
        xorder_id = getIntent().getStringExtra("xorder_id");
    }

    private void give(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
                        new Task1(xorder_id).execute();
                    }
                });
                dialog.setNegativeButton("我再想想", null);
                dialog.show();
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sign_director_signature.clear();
            }
        });

        btn_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sign_director_signature.getTouched()){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setCancelable(false);
                    dialog.setTitle("提示");
                    dialog.setMessage("请主管签名");
                    dialog.setPositiveButton("确定", null);
                    dialog.show();
                    return;
                }
                Bitmap bitmap = sign_director_signature.getBitMap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] bitmapBytes = baos.toByteArray();
                String base64 = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP);
                showProgressDialog();
                new Task2(base64, xorder_id).execute();
            }
        });
    }

    private class Task1 extends AsyncTask {
        private String xorder_id;
        private String r;

        public Task1(String xorder_id){
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
                Intent intent = new Intent(x_center_end.this, Order_Success.class);
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

    private class Task2 extends AsyncTask{

        private String base64;
        private String xorder_id;

        private String r;

        public Task2(String base64, String xorder_id){
            this.base64 = base64;
            this.xorder_id = xorder_id;

        }
        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/x_10";
            String params = "{\"xorder_id\":\"" + xorder_id + "\",\"suffix\":\"_B\",\"director_signature\":\"public/Signature/" + xorder_id + "_B.jpeg\",\"base64\":\"" + base64 + "\",\"account\":\"" + PreferenceManager.getDefaultSharedPreferences(context).getString("account", "") + "\"}";
            Log.d("zzz", params);
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
                Intent intent = new Intent(x_center_end.this, Order_Success.class);
                Bundle bundle = new Bundle();
                bundle.putString("text", "提交成功");
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

    private void dialog_over(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage(StringCollection.STR001);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(x_center_end.this, Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
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


}

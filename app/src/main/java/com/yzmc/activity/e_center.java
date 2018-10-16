package com.yzmc.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.Util_HttpConnect;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class e_center extends AppCompatActivity {

    private Context context = this;
    private String eorder_id;
    private int count = 0;

    private TextView tv_0;
    private Button btn_commit;
    private EditText et_text;

    private LinearLayout ll1;
    private LinearLayout ll2;
    private LinearLayout ll3;
    private LinearLayout ll4;
    private LinearLayout ll5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.e_center);
        AllActivity.addActivity(this);
        init();
        give();
    }

    @Override
    protected void onResume() {
        new Task1(eorder_id).execute();
        super.onResume();
    }

    private void init(){
        tv_0 = findViewById(R.id.tv_0);

        ll1 = findViewById(R.id.ll1);
        ll2 = findViewById(R.id.ll2);
        ll3 = findViewById(R.id.ll3);
        ll4 = findViewById(R.id.ll4);
        ll5 = findViewById(R.id.ll5);

        btn_commit = findViewById(R.id.btn_commit);
        et_text = findViewById(R.id.et_text);

        Intent intent = getIntent();
        eorder_id = intent.getStringExtra("eorder_id");
    }

    private void give(){
        ll1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count != 0){
                    return;
                }
                dialog_modify(1);
            }
        });

        ll2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count < 1){
                    dialog_content("请按顺序更改救援状态");
                    return;
                }
                if(count > 1){
                    return;
                }
                dialog_modify(2);
            }
        });

        ll3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count < 2){
                    dialog_content("请按顺序更改救援状态");
                    return;
                }
                if(count > 2){
                    return;
                }
                dialog_modify(3);
            }
        });

        ll4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count < 3){
                    dialog_content("请按顺序更改救援状态");
                    return;
                }
                if(count > 3){
                    return;
                }
                dialog_modify(4);
            }
        });

        ll5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count < 4){
                    dialog_content("请按顺序更改救援状态");
                    return;
                }
                if(count > 4){
                    return;
                }
                dialog_modify(5);
            }
        });

        btn_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = String.valueOf(et_text.getText()).trim();
                if(text.lastIndexOf("|") >= 0){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setCancelable(false);
                    dialog.setTitle("提示");
                    dialog.setMessage("描述中不能出现\"|\"");
                    dialog.setPositiveButton("确定", null);
                    dialog.show();
                    return;
                }
                if(text.equals("")){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setCancelable(false);
                    dialog.setTitle("提示");
                    dialog.setMessage("当前没有填写进展描述，无需提交");
                    dialog.setPositiveButton("确定", null);
                    dialog.show();
                    return;
                }
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setCancelable(false);
                dialog.setTitle("提示");
                dialog.setMessage("确定要提交当前进展描述吗?提交后不可修改");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Task3(text, eorder_id).execute();
                    }
                });
                dialog.setNegativeButton("我再想想", null);
                dialog.show();
            }
        });
    }

    private class Task1 extends AsyncTask{
        private String eorder_id;
        private String Task1_Result;

        public Task1(String eorder_id){
            this.eorder_id = eorder_id;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/e_3?eorder_id=" + eorder_id;
            Task1_Result = Util_HttpConnect.getGetResult(url, context);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                JSONObject json = new JSONObject(Task1_Result);
                if(json.optInt("code") != 0){
                    dialog_over();
                    return;
                }
                JSONObject jobj = new JSONObject(json.optString("obj"));
                tv_0.setText(fromISODate(jobj.optString("release_time")));

                for(int i = 1; i <= 5; i++){
                    if(jobj.optString("t" + i) != "null"){
                        count ++;
                        String text = fromISODate(jobj.optString("t" + i)) + " " + jobj.optString("p" + i);
                        TextView tv = findViewById(getResources().getIdentifier("tv_" + i, "id", "com.yzmc"));
                        tv.setText(text);
                        ImageView _tv = findViewById(getResources().getIdentifier("iv" + i, "id", "com.yzmc"));
                        if(i <= 4){
                            _tv.setImageDrawable(getResources().getDrawable(R.drawable.em1));
                        }
                        else{
                            _tv.setImageDrawable(getResources().getDrawable(R.drawable.ed1));
                        }
                        continue;
                    }
                    break;
                }

            } catch (Exception e) {
                dialog_over();
                e.printStackTrace();
            }
        }
    }

    private class Task2 extends AsyncTask{
        private int num;
        private String Task2_Result;
        private Date date;
        private String sdate;

        public Task2(int num){
            this.num = num;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdate = sdf.format(date);
            String url = Constant.baseUrl + "api/v1/order/e_4";
            String params = "{\"num\":" + num + ",\"name\":\"" + PreferenceManager.getDefaultSharedPreferences(context).getString("name", "") + "\",\"eorder_id\":\"" + eorder_id + "\",\"time\":\"" + date + "\"}";
            Task2_Result = Util_HttpConnect.getPostResult(url, params, context);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                JSONObject json = new JSONObject(Task2_Result);
                if(json.optInt("code") != 0){
                    dialog_over();
                    return;
                }
                deal(num, sdate);
            } catch (JSONException e) {
                e.printStackTrace();
                dialog_over();
                return;
            }
        }
    }

    private class Task3 extends AsyncTask{

        private String Task3_Result;
        private String text;
        private String eorder_id;

        public Task3(String text, String eorder_id){
            this.text = text;
            this.eorder_id = eorder_id;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/e_5";
            String params = "{\"text\":\"" + text + "\",\"eorder_id\":\"" + eorder_id + "\"}";
            Log.d("zzz", params);
            Task3_Result = Util_HttpConnect.getPostResult(url, params, context);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                Log.d("zzz", Task3_Result);
                JSONObject json = new JSONObject(Task3_Result);
                if(json.optInt("code") != 0){
                    dialog_over();
                    return;
                }
                Intent intent = new Intent(e_center.this, Order_Success.class);
                Bundle bundle = new Bundle();
                bundle.putString("text", "提交成功");
                intent.putExtras(bundle);
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
                dialog_over();
                return;
            }
        }
    }

    private void deal(int num, String sdate){
        count = num;
        String text = sdate + " " + PreferenceManager.getDefaultSharedPreferences(context).getString("name", "");
        TextView tv = findViewById(getResources().getIdentifier("tv_" + num, "id", "com.yzmc"));
        tv.setText(text);
        ImageView _tv = findViewById(getResources().getIdentifier("iv" + num, "id", "com.yzmc"));
        if(num <= 4){
            _tv.setImageDrawable(getResources().getDrawable(R.drawable.em1));
        }
        else{
            _tv.setImageDrawable(getResources().getDrawable(R.drawable.ed1));
        }
    }

    private void dialog_over(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage("服务数据异常,请重新登录");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(e_center.this, Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    private String fromISODate(String time) throws ParseException {
        if(!time.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z")){
            return "";
        }
        time=time.replaceFirst("T", " ").replaceFirst(".\\d{3}Z", "");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse(time);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        cal.add(Calendar.MILLISECOND, (zoneOffset + dstOffset));
        return sdf.format(cal.getTime());
    }

    private void dialog_content(String content){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage(content);
        dialog.setPositiveButton("确定", null);
        dialog.show();
    }

    private void dialog_modify(final int num){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        if(num < 5){
            dialog.setMessage("确定要更改救援状态吗?提交后不可修改");
        }
        if(num == 5){
            dialog.setMessage("确定要更改救援状态吗?提交后不可修改\n此状态为最后一个状态,提交意味着此次救援结束");
        }
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Task2(num).execute();
            }
        });
        dialog.setNegativeButton("我再想想", null);
        dialog.show();
    }
}

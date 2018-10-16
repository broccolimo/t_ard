package com.yzmc.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.Util_HttpConnect;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class e_history_detail extends AppCompatActivity {

    private String eorder_id;
    private TextView tv_0;
    private Context context = this;
    private TextView tv_end;
    private ImageView back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.e_history_detail);
        AllActivity.addActivity(this);
        eorder_id = getIntent().getStringExtra("eorder_id");
        tv_0 = findViewById(R.id.tv_0);
        tv_end = findViewById(R.id.tv_end);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        showProgressDialog();
        new Task1(eorder_id).execute();
        super.onResume();
    }

    private class Task1 extends AsyncTask{

        private String Task1_Result;
        private String eorder_id;

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
                    String text = fromISODate(jobj.optString("t" + i)) + " " + jobj.optString("p" + i);
                    TextView tv = findViewById(getResources().getIdentifier("tv_" + i, "id", "com.yzmc"));
                    tv.setText(text);
                }

                String text = jobj.optString("text");
                String[] arr = text.split("\\|");
                StringBuffer sb = new StringBuffer();
                for(int i = 0; i < arr.length; i++){
                    if(i % 2 == 0){
                        if(arr[i].endsWith("(中国标准时间)")){
                            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT+0800 (中国标准时间)'", Locale.US);
                            SimpleDateFormat _sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            sb.append(_sdf.format(sdf.parse(arr[i])));
                        }
                        if(arr[i].endsWith("(UTC)")){
                            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT+0000 (UTC)'", Locale.US);
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(sdf.parse(arr[i]));
                            calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + 8);
                            SimpleDateFormat _sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            sb.append(_sdf.format(calendar.getTime()));
                        }
                    }
                    else{
                        sb.append(arr[i]);
                    }
                    sb.append("\n");
                }
                tv_end.setText(sb.toString());

            } catch (Exception e) {
                closeProgressDialog();
                dialog_over();
                e.printStackTrace();
            }
            closeProgressDialog();
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
                Intent intent = new Intent(e_history_detail.this, Login.class);
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


    private ProgressDialog progressDialog;
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

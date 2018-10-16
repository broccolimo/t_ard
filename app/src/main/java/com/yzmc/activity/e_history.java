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
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.FaultCode;
import com.yzmc.util.Util_HttpConnect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class e_history extends AppCompatActivity {

    private ListView lv;
    private ImageView back;

    private Context context = this;

    List<Map<String, Object>> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.e_history);
        AllActivity.addActivity(this);
        lv = findViewById(R.id.lv);
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
        new Task1().execute();
        super.onResume();
    }

    private class Task1 extends AsyncTask{

        private String Task1_Result;

        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/e_2?flag=1";
            Task1_Result = Util_HttpConnect.getGetResult(url, context);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try{
                list.clear();
                JSONObject json = new JSONObject(Task1_Result);
                if(json.optInt("code") != 0){
                    dialog_over();
                    return;
                }
                //数据装填
                JSONArray jsonArray = json.getJSONArray("obj");
                for(int i = 0; i < jsonArray.length(); i++){
                    String str = String.valueOf(jsonArray.get(i));
                    JSONObject jstr = new JSONObject(str);
                    Map<String, Object> map = new HashMap<>();
                    map.put("eorder_id", jstr.optString("eorder_id"));
                    map.put("useUnit", jstr.optString("useUnit"));
                    map.put("addr", jstr.optString("addr"));
                    map.put("release_time", fromISODate(jstr.optString("release_time")));
                    list.add(map);
                }
                //数据装填完毕
                //页面渲染
                lv.removeAllViewsInLayout();
                if(list.size() == 0){
                    list.add(null);
                    SimpleAdapter adapter = new SimpleAdapter(context, list, R.layout.item_empty, null, null);
                    lv.setAdapter(adapter);
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        }
                    });
                    closeProgressDialog();
                    return;
                }
                SimpleAdapter adapter = new SimpleAdapter(context, list, R.layout.item6,
                        new String[]{"eorder_id", "useUnit", "addr", "release_time"}, new int[]{R.id.eorder_id, R.id.item_tv_1, R.id.item_tv_2,
                        R.id.item_tv_4});
                lv.setAdapter(adapter);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
                        String temp_eorder_id = String.valueOf(map.get("eorder_id"));
                        Intent intent = new Intent(e_history.this, e_history_detail.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("eorder_id", temp_eorder_id);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
                closeProgressDialog();
            }
            catch (Exception e){
                e.printStackTrace();
                dialog_over();
                return;
            }
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
                Intent intent = new Intent(e_history.this, Login.class);
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
        // 1、取得本地时间：
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
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

package com.yzmc.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.StringCollection;
import com.yzmc.util.Util_HttpConnect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Review extends AppCompatActivity {

    private Context context = this;
    private String result;
    private ListView lv;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review);
        AllActivity.addActivity(this);
        init();
    }

    @Override
    protected void onResume() {
        new PageTask().execute();
        super.onResume();
    }

    private void init(){
        lv = findViewById(R.id.lv);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }



    private class PageTask extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/findAllOrder?flag=3";
            result = Util_HttpConnect.getGetResult(url, context);

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try{
                JSONObject json = new JSONObject(result);
                int code = json.optInt("code");
                if(code != 0){
                    dialog_over();
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("zzz", result);
            loadReviewList();
        }
    }

    private List<Map<String, Object>> getData(){
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            Log.d("zzz", result);
            JSONArray jsonArray = new JSONObject(result).getJSONArray("obj");
            for(int i = 0; i < jsonArray.length(); i++){
                String str = String.valueOf(jsonArray.get(i));
                JSONObject json = new JSONObject(str);
                Map<String, Object> map = new HashMap<>();
                //order_id
                map.put("order_id", json.optString("order_id"));
                //维保单id
                map.put("maintain_id", json.optString("maintain_id"));
                //维保人
                map.put("accept_person", json.optString("accept_person"));
                //使用单位
                map.put("useUnit", json.optString("useUnit"));
                //电梯地址
                map.put("addr", json.optString("addr"));
                //注册代码
                map.put("registerID", json.optString("registerID"));
                //电梯型号
                map.put("productID", json.optString("productID"));
                try {
                    //发单时间
                    map.put("release_time", fromISODate(json.optString("release_time")));
                    //提交时间
                    map.put("commit_time", fromISODate(json.optString("commit_time")));
                } catch (ParseException e) {
                    e.printStackTrace();
                    dialog_over();
                    return null;

                }
                switch(json.optInt("maintain_type")){
                    case 0:
                        map.put("maintain_type", "半月维保");
                        break;
                    case 1:
                        map.put("maintain_type", "季度维保");
                        break;
                    case 2:
                        map.put("maintain_type", "半年维保");
                        break;
                    case 3:
                        map.put("maintain_type", "一年维保");
                        break;
                    default:
                        break;
                }
                list.add(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    //加载审核列表
    private void loadReviewList(){
        lv.removeAllViewsInLayout();
        List<Map<String, Object>> list = getData();
        if(list.size() == 0){
            //AdapterView必须有adapter,可我就是想显示一项固定内容的项
            //只能让这个list的长度为1,添加一项
            list.add(null);
            SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.item_empty, null, null);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });
            return;
        }
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.item3,
                new String[]{"order_id", "maintain_id", "accept_person", "commit_time", "useUnit", "addr", "registerID", "productID",
                        "release_time", "maintain_type"},
                new int[]{R.id.order_id, R.id.maintain_id, R.id.item_tv_1, R.id.item_tv_2,
                R.id.item_tv_3, R.id.item_tv_4, R.id.item_tv_5, R.id.item_tv_6, R.id.item_tv_7, R.id.item_tv_8});
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
                String order_id = String.valueOf(map.get("order_id"));
                String maintain_id = String.valueOf(map.get("maintain_id"));
                String maintain_type = String.valueOf(map.get("maintain_type"));
                Intent intent = new Intent(Review.this, ReviewDetail.class);
                Bundle bundle = new Bundle();
                bundle.putString("order_id", order_id);
                bundle.putString("maintain_id", maintain_id);
                bundle.putString("maintain_type", maintain_type);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }


    //ISO日期转换
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


    private void dialog_over(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage(StringCollection.STR001);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Review.this, Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Review.this, Main.class);
        startActivity(intent);
    }
}

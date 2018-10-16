package com.yzmc.activity;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.yzmc.R;
import com.yzmc.model.OperationModel;
import com.yzmc.model.XOrderArrayModel;
import com.yzmc.service.OperationService;
import com.yzmc.service.OrderService;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.FaultCode;
import com.yzmc.util.MyApplication;
import com.yzmc.util.SetCookie;
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

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class x extends AppCompatActivity implements View.OnClickListener{

    private TextView tv_orderpool;
    private TextView tv_mytask;
    private ImageView back;
    private ListView ll;
    private Context context = this;
    private ProgressDialog progressDialog;
    private List<TextView> labs = new ArrayList<>();

    //工单池数据
    private List<Map<String, Object>> orderpool_data = new ArrayList<>();
    //我的任务数据
    private List<Map<String, Object>> mytask_data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x);
        AllActivity.addActivity(this);
        init();
    }

    private void init(){
        tv_orderpool = findViewById(R.id.tv_orderpool);
        tv_mytask = findViewById(R.id.tv_mytask);
        back = findViewById(R.id.back);
        ll = findViewById(R.id.ll);
        labs.add(tv_orderpool);
        labs.add(tv_mytask);
    }

    @Override
    protected void onResume() {
        showProgressDialog();
        preload();
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_orderpool:
                loadOrderpool();
                break;
            case R.id.tv_mytask:
                loadMytask();
                break;
            case R.id.back:
                Intent intent = new Intent(x.this, Main.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    //加载工单池
    private void loadOrderpool(){
        ll.removeAllViewsInLayout();
        List<Map<String, Object>> list = new ArrayList<>(orderpool_data);
        if(list.size() == 0){
            //AdapterView必须有adapter,可我就是想显示一项固定内容的项
            //只能让这个list的长度为1,添加一项
            list.add(null);
            SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.item_empty, null, null);
            ll.setAdapter(adapter);
            ll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });
            assist(tv_orderpool);
            return;
        }
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.item4,
                new String[]{"xorder_id", "useUnit", "addr", "registerID","elevatorModel", "release_time",
                        "faultName"}, new int[]{R.id.xorder_id, R.id.item_tv_1, R.id.item_tv_2,
                R.id.item_tv_3, R.id.item_tv_4, R.id.item_tv_5, R.id.item_tv_6});
        ll.setAdapter(adapter);
        ll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
                String xorder_id = String.valueOf(map.get("xorder_id"));
                dialog_orderpool(xorder_id);
            }
        });
        assist(tv_orderpool);
    }

    //加载我的任务
    private void loadMytask(){
        //把原来的清空了
        ll.removeAllViewsInLayout();
        List<Map<String, Object>> list = new ArrayList<>(mytask_data);
        if(list.size() == 0){
            list.add(null);
            SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.item_empty, null, null);
            ll.setAdapter(adapter);
            ll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });
            assist(tv_mytask);
            return;
        }
        MyAdapter adapter = new MyAdapter(this, list, R.layout.item5, new String[]{
                "xorder_id", "flag", "useUnit", "addr", "registerID", "elevatorModel", "release_time",
                "faultName"
        }, new int[]{
                R.id.order_id, R.id.flag, R.id.item_tv_1, R.id.item_tv_2,
                R.id.item_tv_3, R.id.item_tv_4, R.id.item_tv_5, R.id.item_tv_6
        });
        ll.setAdapter(adapter);
        ll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
                String xorder_id = String.valueOf(map.get("xorder_id"));
                int flag = (int)map.get("flag");
                deal_mytask(xorder_id, flag);
            }
        });
        assist(tv_mytask);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(x.this, Main.class);
        startActivity(intent);
    }

    //选项卡样式封装
    private void assist(TextView tv){
        for(TextView t : labs){
            if(t == tv){
                t.setBackgroundResource(R.drawable.color_bg_2);
                t.setClickable(false);
                continue;
            }
            t.setBackgroundColor(getResources().getColor(R.color.white));
            t.setClickable(true);
        }
    }

    //工单池项点击事件弹窗
    public void dialog_orderpool(final String xorder_id){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("提示");
        dialog.setMessage("确认要接单吗?");
        dialog.setPositiveButton("是的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgressDialog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new SetCookie()).build();
                        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constant.baseUrl).addConverterFactory(GsonConverterFactory.create()).client(client).build();
                        OperationService operationService = retrofit.create(OperationService.class);
                        Call<OperationModel> call = operationService.acceptXOrder(xorder_id,
                                PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).getString("account", ""),
                                PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).getString("name", ""),
                                new Date()
                        );
                        call.enqueue(new Callback<OperationModel>() {
                            @Override
                            public void onResponse(Call<OperationModel> call, Response<OperationModel> response) {
                                closeProgressDialog();
                                Message msg = new Message();
                                Log.d("jy", response.body().code + "");
                                msg.what = response.body().code;
                                msg.obj = xorder_id;
                                h1.sendMessage(msg);
                            }

                            @Override
                            public void onFailure(Call<OperationModel> call, Throwable t) {
                                closeProgressDialog();
                                t.printStackTrace();
                                dialog_over();
                            }
                        });
                    }
                }).start();
            }
        });
        dialog.setNegativeButton("我再想想", null);
        dialog.setCancelable(false);
        dialog.show();
    }

    //处理我的任务点击事件
    private void deal_mytask(final String xorder_id, int flag){
        Intent intent;
        Bundle bundle;
        switch (flag){
            case 1:
                showProgressDialog();
                intent = new Intent(x.this, x_map.class);
                bundle = new Bundle();
                bundle.putString("xorder_id", xorder_id);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case 2:
                showProgressDialog();
                intent = new Intent(x.this, x_center.class);
                bundle = new Bundle();
                bundle.putString("xorder_id", xorder_id);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case 4:
                AlertDialog.Builder ab = new AlertDialog.Builder(context);
                ab.setTitle("提示");
                ab.setMessage("确定要继续维修吗?");
                ab.setCancelable(false);
                ab.setNegativeButton("我再想想", null);
                ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgressDialog();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new SetCookie()).build();
                                Retrofit retrofit = new Retrofit.Builder().baseUrl(Constant.baseUrl)
                                        .addConverterFactory(GsonConverterFactory.create())
                                        .client(client).build();
                                OperationService operationService = retrofit.create(OperationService.class);
                                Call<OperationModel> call =operationService.continueXOrder(
                                        PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).getString("account", ""),
                                        xorder_id,
                                        new Date()
                                );
                                call.enqueue(new Callback<OperationModel>() {
                                    @Override
                                    public void onResponse(Call<OperationModel> call, Response<OperationModel> response) {
                                        closeProgressDialog();
                                        Message msg = new Message();
                                        msg.what = response.body().code;
                                        msg.obj = xorder_id;
                                        h2.sendMessage(msg);
                                    }

                                    @Override
                                    public void onFailure(Call<OperationModel> call, Throwable t) {
                                        closeProgressDialog();
                                        Message msg = new Message();
                                        msg.what = -1;
                                        h2.sendMessage(msg);
                                    }
                                });
                            }
                        }).start();
                    }
                });
                ab.show();
            default:
                break;
        }
    }

    //不能接工单 弹窗
    public void dialog_content(String content){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("提示");
        dialog.setMessage(content);
        dialog.setPositiveButton("确定", null);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void dialog_over(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage(StringCollection.STR001);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(x.this, Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    //我的任务 ListView适配器
    private class MyAdapter extends SimpleAdapter{

        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView t = view.findViewById(R.id.flag);
            TextView tv = view.findViewById(R.id.status);
            int flag = Integer.parseInt(t.getText().toString());
            if(flag == 1){
                tv.setText("未开始...");
            }
            if(flag == 2){
                tv.setText("进行中...");
            }
            if(flag == 4){
                tv.setText("延时中...");
            }
            return view;
        }
    }

    //预加载
    private void preload(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new SetCookie()).build();
                Retrofit retrofit = new Retrofit.Builder().baseUrl(Constant.baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client).build();
                OrderService orderService = retrofit.create(OrderService.class);
                Call<XOrderArrayModel> call = orderService.showXOrders(PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).getString("account", ""));
                call.enqueue(new Callback<XOrderArrayModel>() {
                    @Override
                    public void onResponse(Call<XOrderArrayModel> call, Response<XOrderArrayModel> response) {
                        if(response.body().code != 0){
                            Log.d("jy", response.body().code + " : " + response.body());
                            encapsulate();
                            return;
                        }
                        XOrderArrayModel.xorderInfo[] xorderInfos = response.body().obj;
                        for(int i = 0; i < xorderInfos.length; i++){
                            Map<String,Object> map = new HashMap<>();
                            map.put("xorder_id", xorderInfos[i].xorder_id);
                            map.put("useUnit", xorderInfos[i].useUnit);
                            map.put("addr", xorderInfos[i].addr);
                            map.put("registerID", xorderInfos[i].registerID);
                            map.put("elevatorModel", xorderInfos[i].elevatorModel);
                            try{
                                map.put("release_time", fromISODate(xorderInfos[i].release_time));
                            }
                            catch (Exception e){
                                e.printStackTrace();
                                map.put("release_time", "");
                            }
                            map.put("faultName", FaultCode.getFaultName(xorderInfos[i].faultCode));
                            orderpool_data.add(map);
                        }

                        tv_orderpool.setClickable(false);
                        tv_orderpool.setOnClickListener(x.this);
                        tv_orderpool.setBackgroundResource(R.drawable.color_bg_2);
                        tv_mytask.setOnClickListener(x.this);
                        back.setOnClickListener(x.this);
                        closeProgressDialog();
                        loadOrderpool();
                    }

                    @Override
                    public void onFailure(Call<XOrderArrayModel> call, Throwable t) {
                        t.printStackTrace();
                        encapsulate();
                    }
                });
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new SetCookie()).build();
                Retrofit retrofit = new Retrofit.Builder().baseUrl(Constant.baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(client).build();
                OrderService orderService = retrofit.create(OrderService.class);
                Call<XOrderArrayModel> call = orderService.showMyTaskX(PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).getString("account", ""));
                call.enqueue(new Callback<XOrderArrayModel>() {
                    @Override
                    public void onResponse(Call<XOrderArrayModel> call, Response<XOrderArrayModel> response) {
                        if(response.body().code != 0){
                            Log.d("jy", response.body().code + " : " + response.body().err);
                            encapsulate();
                            return;
                        }
                        XOrderArrayModel.xorderInfo[] xorderInfos = response.body().obj;
                        for(int i = 0; i < xorderInfos.length; i++){
                            Map<String,Object> map = new HashMap<>();
                            map.put("xorder_id", xorderInfos[i].xorder_id);
                            map.put("flag", xorderInfos[i].flag);
                            map.put("useUnit", xorderInfos[i].useUnit);
                            map.put("addr", xorderInfos[i].addr);
                            map.put("registerID", xorderInfos[i].registerID);
                            map.put("elevatorModel", xorderInfos[i].elevatorModel);
                            try{
                                map.put("release_time", fromISODate(xorderInfos[i].release_time));
                            }
                            catch (Exception e){
                                e.printStackTrace();
                                map.put("release_time", "");
                            }
                            map.put("faultName", FaultCode.getFaultName(xorderInfos[i].faultCode));
                            mytask_data.add(map);
                        }

                    }

                    @Override
                    public void onFailure(Call<XOrderArrayModel> call, Throwable t) {
                        t.printStackTrace();
                        encapsulate();
                    }
                });
            }
        }).start();
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

    //预加载代码封装
    private void encapsulate(){
        closeProgressDialog();
        Message msg = new Message();
        msg.what = 0;
        h0.sendMessage(msg);
    }

    //预加载时发生错误时弹窗
    private Handler h0 = new Handler(){
        private boolean flag = true;
        @Override
        public void handleMessage(Message msg) {
            if(flag && msg.what == 0){
                flag = false;
                dialog_over();
            }
        }
    };

    //处理接单事件
    private Handler h1 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case -1:
                    dialog_over();
                    break;
                case 0:
                    Intent intent = new Intent(x.this, x_map.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("xorder_id", String.valueOf(msg.obj));
                    intent.putExtras(bundle);
                    startActivity(intent);
                    break;
                case 1:
                    dialog_content("您当前还有维修单没有完成，不能继续接单");
                    break;
                case 2:
                    dialog_content("此维修单以被他人接取");
                    onResume();
                    break;
                default:
                    break;
            }
        }
    };

    //处理继续维修
    private Handler h2 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == -1){
                dialog_over();
                return;
            }
            if(msg.what == 1){
                dialog_content("您当前有维修任务正在进行,请完成后再继续此单");
                return;
            }
            if(msg.what == 0){
                Intent intent = new Intent(x.this, x_map.class);
                Bundle bundle = new Bundle();
                bundle.putString("xorder_id", String.valueOf(msg.obj));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    };
}



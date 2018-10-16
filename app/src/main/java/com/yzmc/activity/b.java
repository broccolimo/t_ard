
package com.yzmc.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.yzmc.R;
import com.yzmc.model.OperationModel;
import com.yzmc.model.OrderArrayModel;
import com.yzmc.service.OperationService;
import com.yzmc.service.OrderService;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.MyApplication;
import com.yzmc.util.SetCookie;
import com.yzmc.util.StringCollection;
import com.yzmc.util.Util_HttpConnect;

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

public class b extends AppCompatActivity implements View.OnClickListener{

    //列表控件
    private ListView listView;
    //工单池选项卡
    private TextView tv_orderpool;
    //我的任务选项卡
    private TextView tv_mytask;
    //左上角返回
    private ImageView back;
    private Context context = this;
    private List<TextView> labs = new ArrayList<>();
    private ProgressDialog progressDialog = null;


    //当工单池数据或我的任务数据获取出现意想不到的错误时 显示这个
    private AlertDialog.Builder showError = null;
    private boolean showErrorFlag = true;

    //工单池数据
    private List<Map<String, Object>> orderpool_data = null;
    //我的任务数据
    private List<Map<String, Object>> mytask_data = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.b);
        AllActivity.addActivity(this);
        init();
    }

    private void init(){
        tv_orderpool = findViewById(R.id.tv_orderpool);
        tv_orderpool.setOnClickListener(this);
        tv_mytask = findViewById(R.id.tv_mytask);
        tv_mytask.setOnClickListener(this);
        listView = findViewById(R.id.listView1);
        back = findViewById(R.id.back);
        back.setOnClickListener(this);
        labs.add(tv_orderpool);
        labs.add(tv_mytask);

        showError = new AlertDialog.Builder(this);
        showError.setCancelable(false);
        showError.setTitle("提示");
        showError.setMessage(StringCollection.STR001);
        showError.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(b.this, Login.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        showProgressDialog();

        //加载工单池数据的线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(new SetCookie())
                        .build();
                Retrofit retrofit = new Retrofit.Builder().baseUrl(Constant.baseUrl)
                        .addConverterFactory(GsonConverterFactory.create()).client(client).build();
                OrderService orderService = retrofit.create(OrderService.class);
                Call<OrderArrayModel> call = orderService.showOrders(PreferenceManager.
                    getDefaultSharedPreferences(MyApplication.getContext()).getString("account", ""));
                call.enqueue(new Callback<OrderArrayModel>() {
                    @Override
                    public void onResponse(Call<OrderArrayModel> call, Response<OrderArrayModel> response) {
                        if(response.body().code != 0){
                            closeProgressDialog();
                            showError();
                            return;
                        }
                        OrderArrayModel.OrderInfo[] orderInfos = response.body().obj;
                        orderpool_data = new ArrayList<>();
                        for(int i = 0; i < orderInfos.length; i++){
                            Map<String, Object> map = new HashMap<>();
                            map.put("order_id", orderInfos[i].order_id);
                            map.put("useUnit", orderInfos[i].useUnit);
                            map.put("addr", orderInfos[i].addr);
                            map.put("registerID", orderInfos[i].registerID);
                            map.put("productID", orderInfos[i].productID);
                            try {
                                map.put("release_time", fromISODate(orderInfos[i].release_time));

                            } catch (ParseException e) {
                                map.put("release_time", "");
                                e.printStackTrace();
                            }
                            switch(orderInfos[i].maintain_type){
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
                            orderpool_data.add(map);
                        }
                        loadOrderpool();
                        closeProgressDialog();
                    }
                    @Override
                    public void onFailure(Call<OrderArrayModel> call, Throwable t) {
                        closeProgressDialog();
                        t.printStackTrace();
                        showError();
                    }
                });
            }
        }).start();


        //加载我的任务数据的线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(new SetCookie())
                        .build();
                Retrofit retrofit = new Retrofit.Builder().baseUrl(Constant.baseUrl)
                        .addConverterFactory(GsonConverterFactory.create()).client(client).build();
                OrderService orderService = retrofit.create(OrderService.class);
                Call<OrderArrayModel> call = orderService.getThisInfo(PreferenceManager.
                        getDefaultSharedPreferences(MyApplication.getContext()).getString("account", ""));
                call.enqueue(new Callback<OrderArrayModel>() {
                    @Override
                    public void onResponse(Call<OrderArrayModel> call, Response<OrderArrayModel> response) {
                        if(response.body().code != 0){
                            closeProgressDialog();
                            showError();
                            return;
                        }
                        OrderArrayModel.OrderInfo[] orderInfos = response.body().obj;
                        mytask_data = new ArrayList<>();
                        for(int i = 0; i < orderInfos.length; i++){
                            Map<String, Object> map = new HashMap<>();
                            map.put("order_id", orderInfos[i].order_id);
                            map.put("flag", orderInfos[i].flag);
                            map.put("useUnit", orderInfos[i].useUnit);
                            map.put("addr", orderInfos[i].addr);
                            map.put("registerID", orderInfos[i].registerID);
                            map.put("productID", orderInfos[i].productID);
                            map.put("refuse_reason", orderInfos[i].refuse_reason);
                            map.put("delay_description", orderInfos[i].delay_description);
                            try {
                                map.put("release_time", fromISODate(orderInfos[i].release_time));
                            }
                            catch (Exception e) {
                                map.put("release_time", "");
                            }
                            try {
                                map.put("refuse_time", fromISODate(orderInfos[i].release_time));
                            }
                            catch (Exception e) {
                                map.put("refuse_time", "");
                            }
                            try {
                                map.put("delay_time", fromISODate(orderInfos[i].release_time));
                            }
                            catch (Exception e) {
                                map.put("delay_time", "");
                            }
                            switch(orderInfos[i].maintain_type){
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
                            mytask_data.add(map);
                        }
                    }
                    @Override
                    public void onFailure(Call<OrderArrayModel> call, Throwable t) {
                        closeProgressDialog();
                        t.printStackTrace();
                        showError();
                    }
                });
            }
        }).start();
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
                onBackPressed();
                break;
            default:
                break;
        }
    }

    public void showError(){
        if(showErrorFlag){
            showErrorFlag = false;
            showError.show();
        }
    }

    //加载工单池
    private void loadOrderpool(){
        listView.removeAllViewsInLayout();
        if(orderpool_data.size() == 0){
            List<Map<String, Object>> tempList = new ArrayList<>();
            tempList.add(null);
            SimpleAdapter adapter = new SimpleAdapter(this, tempList, R.layout.item_empty, null, null);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });
            assist(tv_orderpool);
            return;
        }
        SimpleAdapter adapter = new SimpleAdapter(this, orderpool_data, R.layout.item1,
                new String[]{"order_id", "useUnit", "addr", "registerID","productID", "release_time",
                        "maintain_type"}, new int[]{R.id.order_id, R.id.item_tv_1, R.id.item_tv_2,
                R.id.item_tv_3, R.id.item_tv_4, R.id.item_tv_5, R.id.item_tv_6});
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
                String order_id = String.valueOf(map.get("order_id"));
                dialog_orderpool(order_id);
            }
        });
        assist(tv_orderpool);
    }

    //加载我的任务
    private void loadMytask(){
        listView.removeAllViewsInLayout();
        if(mytask_data.size() == 0){
            List<Map<String, Object>> tempList = new ArrayList<>();
            tempList.add(null);
            SimpleAdapter adapter = new SimpleAdapter(this, tempList, R.layout.item_empty, null, null);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });
            assist(tv_mytask);
            return;
        }
        MyAdapter adapter = new MyAdapter(this, mytask_data, R.layout.item2, new String[]{
                "order_id", "flag", "", "useUnit", "addr", "registerID","productID", "release_time",
                "maintain_type", "refuse_time", "refuse_reason", "delay_time", "delay_description"
        }, new int[]{
                R.id.order_id, R.id.hidden_flag, R.id.status, R.id.item_tv_1, R.id.item_tv_2,
                R.id.item_tv_3, R.id.item_tv_4, R.id.item_tv_5, R.id.item_tv_6, R.id.item_tv_7,
                R.id.item_tv_8, R.id.item_tv_9, R.id.item_tv_10
        });
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
                String order_id = String.valueOf(map.get("order_id"));
                int flag = (int)map.get("flag");
                //审核不通过
                String refuse_time = "";
                String refuse_reason = "";
                if(flag == 4){
                    refuse_time = String.valueOf(map.get("refuse_time"));
                    refuse_reason = String.valueOf(map.get("refuse_reason"));
                }
                //延时
                String delay_time = "";
                String delay_description = "";
                if(flag == 6){
                    delay_time = String.valueOf(map.get("delay_time"));
                    delay_description = String.valueOf(map.get("delay_description"));
                }
                deal_mytask(order_id, flag, refuse_time, refuse_reason, delay_time, delay_description);
            }
        });
        assist(tv_mytask);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(b.this, Main.class);
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
    public void dialog_orderpool(final String order_id){
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
                        OkHttpClient client = new OkHttpClient.Builder()
                                .addInterceptor(new SetCookie())
                                .build();
                        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constant.baseUrl)
                                .addConverterFactory(GsonConverterFactory.create()).client(client).build();
                        OperationService operationService = retrofit.create(OperationService.class);
                        Call<OperationModel> call = operationService.acceptBOrder(
                                PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).getString("account", ""),
                                PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).getString("name", ""),
                                order_id,
                                new Date(),
                                PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).getString("company", ""));
                        call.enqueue(new Callback<OperationModel>() {
                            @Override
                            public void onResponse(Call<OperationModel> call, Response<OperationModel> response) {
                                if(response.body().code == -1){
                                    dialog_over();
                                    return;
                                }
                                if(response.body().code == 1){
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                                    dialog.setCancelable(false);
                                    dialog.setTitle("提示");
                                    dialog.setMessage("您当前有进行中的维保单,不能继续接单");
                                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            onResume();
                                        }
                                    });
                                    dialog.show();
                                    return;
                                }
                                if(response.body().code == 2){
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                                    dialog.setCancelable(false);
                                    dialog.setTitle("提示");
                                    dialog.setMessage("该工单已被他人接取");
                                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            onResume();
                                        }
                                    });
                                    dialog.show();
                                    return;
                                }
                                Intent intent = new Intent(b.this, b_map.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("order_id", order_id);
                                intent.putExtras(bundle);
                                startActivity(intent);
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
    private void deal_mytask(final String order_id, int flag, String refuse_time, String refuse_reason, String delay_time, String delay_description){
        Intent intent;
        Bundle bundle;
        switch (flag){
            //未开始
            case 1:
                showProgressDialog();
                intent = new Intent(b.this, b_map.class);
                bundle = new Bundle();
                bundle.putString("order_id", order_id);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            //进行中
            case 2:
                intent = new Intent(b.this, b_workContent.class);
                bundle = new Bundle();
                bundle.putString("order_id", order_id);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case 4:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示");
                builder.setCancelable(false);
                final AlertDialog dialog = builder.create();

                View view = LayoutInflater.from(context).inflate(R.layout.restart, null);
                TextView tv_refuse_time = view.findViewById(R.id.refuse_time);
                tv_refuse_time.setText(refuse_time);
                TextView tv_refuse_reason = view.findViewById(R.id.refuse_reason);
                tv_refuse_reason.setText(refuse_reason);
                TextView tv_time = view.findViewById(R.id.time);
                tv_time.setText("审核时间:");
                TextView tv_reason = view.findViewById(R.id.reason);
                tv_reason.setText("不通过原因:");

                Button btn_restart = view.findViewById(R.id.btn_restart);
                btn_restart.setText("重新维保");
                btn_restart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder ab = new AlertDialog.Builder(context);
                        ab.setTitle("提示");
                        ab.setMessage("确定要重新维保吗?");
                        ab.setCancelable(false);
                        ab.setNegativeButton("我再想想", null);
                        ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //如果当前用户有单子在进行 就不能重新维保
                                if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("canAccept_b", false) == false){
                                    AlertDialog.Builder _dialog = new AlertDialog.Builder(context);
                                    _dialog.setCancelable(false);
                                    _dialog.setTitle("提示");
                                    _dialog.setMessage("您当前还有订单没有完成,不能重新维保");
                                    _dialog.setPositiveButton("确定", null);
                                    _dialog.show();
                                    return;
                                }
                                //没有 则可以重新维保
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String url = Constant.baseUrl + "api/v1/order/Opt_13";
                                        String params = "{\"accept_time\":\"" + new Date() +
                                                "\",\"order_id\":\"" + order_id +
                                                "\",\"account\":\"" + PreferenceManager.getDefaultSharedPreferences(context).getString("account", "") +
                                                "\"}";
                                        String res = Util_HttpConnect.getPostResult(url, params, context);
                                        try {
                                            JSONObject json = new JSONObject(res);
                                            int code = json.optInt("code");
                                            Message msg = new Message();
                                            msg.what = code;
                                            msg.obj = order_id;
                                            mHandler_1.sendMessage(msg);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            dialog_over();
                                            return;
                                        }
                                    }
                                }).start();
                            }
                        });
                        ab.show();
                    }
                });
                Button btn_cancel = view.findViewById(R.id.btn_cancel);
                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.setView(view);
                dialog.show();
                break;
            case 6:
                final AlertDialog.Builder builder6 = new AlertDialog.Builder(this);
                builder6.setTitle("提示");
                builder6.setCancelable(false);
                final AlertDialog dialog6 = builder6.create();

                View view6 = LayoutInflater.from(context).inflate(R.layout.restart, null);
                TextView tv_refuse_time6 = view6.findViewById(R.id.refuse_time);
                tv_refuse_time6.setText(delay_time);
                TextView tv_refuse_reason6 = view6.findViewById(R.id.refuse_reason);
                tv_refuse_reason6.setText(delay_description);
                TextView tv_time6 = view6.findViewById(R.id.time);
                tv_time6.setText("延时开始时间:");
                TextView tv_reason6 = view6.findViewById(R.id.reason);
                tv_reason6.setText("延时原因:");

                Button btn_restart6 = view6.findViewById(R.id.btn_restart);
                btn_restart6.setText("继续维保");
                btn_restart6.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder ab = new AlertDialog.Builder(context);
                        ab.setTitle("提示");
                        ab.setMessage("确定要继续维保吗?");
                        ab.setCancelable(false);
                        ab.setNegativeButton("我再想想", null);
                        ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("canAccept_b", false) == false){
                                    AlertDialog.Builder _dialog = new AlertDialog.Builder(context);
                                    _dialog.setCancelable(false);
                                    _dialog.setTitle("提示");
                                    _dialog.setMessage("您当前还有订单没有完成,不能继续维保");
                                    _dialog.setPositiveButton("确定", null);
                                    _dialog.show();
                                    return;
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String url = Constant.baseUrl + "api/v1/order/Opt_16";
                                        String params = "{\"accept_time\":\"" + new Date() +
                                                "\",\"order_id\":\"" + order_id +
                                                "\",\"account\":\"" + PreferenceManager.getDefaultSharedPreferences(context).getString("account", "") +
                                                "\"}";
                                        String res = Util_HttpConnect.getPostResult(url, params, context);
                                        try {
                                            JSONObject json = new JSONObject(res);
                                            int code = json.optInt("code");
                                            Message msg = new Message();
                                            msg.what = code;
                                            msg.obj = order_id;
                                            mHandler_1.sendMessage(msg);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            dialog_over();
                                            return;
                                        }
                                    }
                                }).start();
                            }
                        });
                        ab.show();
                    }
                });
                Button btn_cancel6 = view6.findViewById(R.id.btn_cancel);
                btn_cancel6.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog6.dismiss();
                    }
                });
                dialog6.setView(view6);
                dialog6.show();
                break;
            default:
                break;
        }
    }

    //不能接工单 弹窗
    public void dialog_cannot_orderTaking(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("提示");
        dialog.setMessage("您当前还有订单没有完成，不能继续接单");
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
                Intent intent = new Intent(b.this, Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
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
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        // 2、取得时间偏移量：
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        // 4、从本地时间里扣除这些差量，即可以取得UTC时间：
        cal.add(Calendar.MILLISECOND, (zoneOffset + dstOffset));
        return sdf.format(cal.getTime());
    }

    //我的任务 Adapter
    private class MyAdapter extends SimpleAdapter{

        public MyAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView t = view.findViewById(R.id.hidden_flag);
            TextView tv = view.findViewById(R.id.status);
            int flag = Integer.parseInt(t.getText().toString());
            if(flag == 1){
                tv.setText("未开始...");
            }
            if(flag == 2){
                tv.setText("进行中...");
            }
            if(flag == 4){
                tv.setText("审核未通过...");
            }
            if(flag == 6){
                tv.setText("延时中...");
            }
            return view;
        }
    }

    private Handler mHandler_1 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what != 0){
                dialog_over();
                return;
            }
            showProgressDialog();
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("canAccept_b", false).commit();
            Intent intent = new Intent(b.this, b_map.class);
            Bundle bundle = new Bundle();
            bundle.putString("order_id", String.valueOf(msg.obj));
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };

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

    private void closeProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private Handler H1 = new Handler(){
        private boolean flag1 = false;
        private boolean flag2 = false;

        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 9999){
                flag1 = true;
            }
            if(msg.what == 9998){
                flag2 = true;
            }
        }
    };

}

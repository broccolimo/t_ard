package com.yzmc.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import com.yzmc.R;
import com.yzmc.fragment.ElevatorInfo;
import com.yzmc.fragment.Maintenance;
import com.yzmc.model.DeviceModel;
import com.yzmc.model.OrderModel;
import com.yzmc.service.DeviceService;
import com.yzmc.service.OrderService;
import com.yzmc.util.AFCallBack;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.SerializableMap;
import com.yzmc.util.SetCookie;
import com.yzmc.util.StringCollection;
import com.yzmc.util.Util_HttpConnect;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class b_center extends AppCompatActivity implements View.OnClickListener, ElevatorInfo.MyListener1, Maintenance.MyListener2{
    private String maintenceInfo;
    private String order_id;
    private String maintain_id;
    private int maintain_type;

    private TextView item1;
    private TextView item2;

    private ElevatorInfo fragment1;
    private Maintenance fragment2;

    private List<TextView> labs = new ArrayList<>();
    private Context context = this;

    private boolean isFirstResume = true;

    private Map<String, Object> elevatorInfo;

    private Timer timer;
    private boolean flag1 = false;
    private boolean flag2 = false;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.b_center);
        AllActivity.addActivity(this);
        item1 = findViewById(R.id.item1);
        item2 = findViewById(R.id.item2);
        labs.add(item1);
        labs.add(item2);
        item1.setOnClickListener(this);
        item2.setOnClickListener(this);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        Intent intent = getIntent();
        order_id = intent.getStringExtra("order_id");

    }

    @Override
    public void sendMessage1(int code) {
        if(code == 0){
            flag1 = true;
        }
    }

    @Override
    public void sendMessage2(int code) {
        if(code == 0){
            flag2 = true;
        }
    }

    @Override
    protected void onResume() {
        if(isFirstResume == true){
            showProgressDialog();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(new SetCookie())
                            .build();
                    Retrofit retrofit = new Retrofit.Builder().baseUrl(Constant.baseUrl)
                            .addConverterFactory(GsonConverterFactory.create()).client(client).build();
                    DeviceService deviceService = retrofit.create(DeviceService.class);
                    Call<DeviceModel> call = deviceService.getDeviceInfo(order_id);
                    call.enqueue(new Callback<DeviceModel>() {
                        @Override
                        public void onResponse(Call<DeviceModel> call, Response<DeviceModel> response) {
                            if(response.body().code != 0){
                                Message msg = new Message();
                                msg.what = -1;
                                h1.sendMessage(msg);
                                return;
                            }
                            //装填电梯数据
                            elevatorInfo = new HashMap<>();
                            elevatorInfo.put("useUnitID", response.body().obj.useUnitID == null ? "" : response.body().obj.useUnitID);
                            elevatorInfo.put("productID", response.body().obj.productID == null ? "" : response.body().obj.productID);
                            elevatorInfo.put("makeUnit", response.body().obj.makeUnit == null ? "" : response.body().obj.makeUnit);
                            elevatorInfo.put("installUnit", response.body().obj.installUnit == null ? "" : response.body().obj.installUnit);
                            elevatorInfo.put("repairUnit", response.body().obj.repairUnit == null ? "" : response.body().obj.repairUnit);
                            elevatorInfo.put("maintainUnit", response.body().obj.maintainUnit == null ? "" : response.body().obj.maintainUnit);
                            elevatorInfo.put("elevatorModel", response.body().obj.elevatorModel == null ? "" : response.body().obj.elevatorModel);
                            elevatorInfo.put("motorPower", response.body().obj.motorPower == null ? "" : response.body().obj.motorPower);
                            elevatorInfo.put("controlBox", response.body().obj.controlBox == null ? "" : response.body().obj.controlBox);
                            elevatorInfo.put("brakes", response.body().obj.brakes == null ? "" : response.body().obj.brakes);
                            elevatorInfo.put("speedLimiter", response.body().obj.speedLimiter == null ? "" : response.body().obj.speedLimiter);
                            elevatorInfo.put("safetyGear", response.body().obj.safetyGear == null ? "" : response.body().obj.safetyGear);
                            elevatorInfo.put("doorLock", response.body().obj.doorLock == null ? "" : response.body().obj.doorLock);
                            elevatorInfo.put("ratedSpeed", response.body().obj.ratedSpeed == null ? "" : response.body().obj.ratedSpeed);
                            elevatorInfo.put("ratedLoad", response.body().obj.ratedLoad == null ? "" : response.body().obj.ratedLoad);
                            elevatorInfo.put("transformModel", response.body().obj.transformModel == null ? "" : response.body().obj.transformModel);
                            elevatorInfo.put("annualInspection", response.body().obj.annualInspection == null ? "" : response.body().obj.annualInspection);
                            elevatorInfo.put("num", response.body().obj.tractionMachine.num == null ? "" : response.body().obj.tractionMachine.num);
                            elevatorInfo.put("diameter", response.body().obj.tractionMachine.diameter == null ? "" : response.body().obj.tractionMachine.diameter);
                            elevatorInfo.put("floor", response.body().obj.layers.floor == null ? "" : response.body().obj.layers.floor);
                            elevatorInfo.put("station", response.body().obj.layers.station == null ? "" : response.body().obj.layers.station);
                            elevatorInfo.put("door", response.body().obj.layers.door == null ? "" : response.body().obj.layers.door);

                            SerializableMap map = new SerializableMap();
                            map.setMap(elevatorInfo);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("map", map);
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragment1 = new ElevatorInfo();
                            fragment1.setArguments(bundle);
                            fragmentTransaction.add(R.id.container, fragment1);
                            fragmentTransaction.commit();
                        }

                        @Override
                        public void onFailure(Call<DeviceModel> call, Throwable t) {
                            t.printStackTrace();
                            Message msg = new Message();
                            msg.what = -1;
                            h1.sendMessage(msg);
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
                            .client(client)
                            .build();
                    OrderService orderService = retrofit.create(OrderService.class);
                    Call<OrderModel> call = orderService.getOrderInfo(order_id);
                    call.enqueue(new Callback<OrderModel>() {
                        @Override
                        public void onResponse(Call<OrderModel> call, Response<OrderModel> response) {
                            if(response.body().code != 0){
                                Message msg = new Message();
                                msg.what = -1;
                                h1.sendMessage(msg);
                                return;
                            }
                            maintain_id = response.body().obj.maintain_id;
                            maintain_type = response.body().obj.maintain_type;
                            //现在已经拿到了维保单的id和类型 然后去请求这张维保单的数据
                            //维保单有点复杂 暂时改造不了
                            new Task().execute();

                        }

                        @Override
                        public void onFailure(Call<OrderModel> call, Throwable t) {
                            t.printStackTrace();
                            Message msg = new Message();
                            msg.what = -1;
                            h1.sendMessage(msg);
                        }
                    });
                }
            }).start();

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(flag1 && flag2){
                        closeProgressDialog();
                        timer.cancel();
                        timer = null;
                    }
                }
            }, 0, 500);

            isFirstResume = false;

        }

        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.item1:
                load_item1();
                break;
            case R.id.item2:
                load_item2();
                break;
            default:
                break;

        }
    }

    private void load_item1(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragment2).show(fragment1).commit();
        assist(item1);
    }

    private void load_item2(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(fragment1).show(fragment2).commit();
        assist(item2);

    }


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

    private void dialog_over(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage(StringCollection.STR001);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(b_center.this, Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    //3个线程只要有一个出问题 就直接显示数据异常
    private Handler h1 = new Handler(){

        private boolean lock = true;
        @Override
        public void handleMessage(Message msg) {
            if(lock && msg.what == -1){
                lock = false;
                closeProgressDialog();
                dialog_over();
            }
        }
    };

    //用于判断fragment是否加载完毕
    private Handler h2 = new Handler(){
        private boolean flag = false;

        @Override
        public void handleMessage(Message msg) {
            if(flag && msg.what == 0){
                //closeProgressDialog();
            }
            if(msg.what == 0){
                flag = true;
            }
        }
    };

    private class Task extends AsyncTask{

        private String result;
        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/maintenance" + maintain_type + "?maintain_id=" + maintain_id;
            result = Util_HttpConnect.getGetResult(url, context);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                JSONObject json = new JSONObject(result);
                if(json.getInt("code") != 0){
                    Message msg = new Message();
                    msg.what = -1;
                    h1.sendMessage(msg);
                    return;
                }
                String temp = json.getString("obj");
                JSONObject json2 = new JSONObject(temp);
                maintenceInfo = json2.getString("maintainContent");
                fragment2 = new Maintenance();
                Bundle bundle = new Bundle();
                bundle.putString("maintenceInfo", maintenceInfo);
                bundle.putString("maintain_id", maintain_id);
                bundle.putInt("maintain_type", maintain_type);
                bundle.putString("order_id", order_id);
                fragment2.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.container, fragment2).hide(fragment2).commit();

            } catch (JSONException e) {
                e.printStackTrace();
                Message msg = new Message();
                msg.what = -1;
                h1.sendMessage(msg);
            }
        }
    }

    private ProgressDialog progressDialog;
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(b_center.this);
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(b_center.this, b.class);
        startActivity(intent);
    }
}

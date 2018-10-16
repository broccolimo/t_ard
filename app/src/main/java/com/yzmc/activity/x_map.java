package com.yzmc.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.MyApplication;
import com.yzmc.util.PositionUtil;
import com.yzmc.util.StringCollection;
import com.yzmc.util.Util_HttpConnect;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class x_map extends AppCompatActivity implements View.OnClickListener {

    private MapView mapView;
    private BaiduMap baiduMap;
    private Button btn_order_start;
    private Button btn_nav;
    private Button btn_delay;
    private TextView tv_1;
    private Double longitude;
    private Double latitude;
    private String xorder_id;
    private Context context = this;
    private Double _longitude;
    private Double _latitude;
    private MyLocationListener myListener = new MyLocationListener();
    private LocationClient locationClient = null;
    private ProgressDialog progressDialog;
    private String addr;
    private String elevatorModel;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.x_map);
        AllActivity.addActivity(this);
        btn_order_start = findViewById(R.id.btn_order_start);
        btn_order_start.setOnClickListener(this);
        btn_nav = findViewById(R.id.nav);
        btn_nav.setOnClickListener(this);
        btn_delay = findViewById(R.id.btn_delay);
        btn_delay.setOnClickListener(this);
        Intent intent = getIntent();
        xorder_id = intent.getStringExtra("xorder_id");
    }

    @Override
    protected void onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(x_map.this,"自Android 6.0开始需要打开位置权限",Toast.LENGTH_SHORT).show();
                }
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        0);
            }
        }
        new Task1(xorder_id).execute();
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_order_start:
                LatLng ele_pos = new LatLng(latitude, longitude);
                LatLng per_pos = new LatLng(_latitude, _longitude);
                Double distance = DistanceUtil.getDistance(ele_pos, per_pos);
                if(distance < 1){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("提示");
                    dialog.setMessage("您与电梯距离较远，不能开始");
                    dialog.setPositiveButton("确定", null);
                    dialog.setCancelable(false);
                    dialog.show();
                    return;
                }
                showProgressDialog();
                new Task2(xorder_id).execute();
                break;
            case R.id.nav:
                if(isInstallBaiduMap("com.baidu.BaiduMap")){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(x_map.this);
                    dialog.setCancelable(false);
                    dialog.setTitle("提示");
                    dialog.setMessage("即将离开电梯云网,打开\"百度地图\"");
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setData(Uri.parse("baidumap://map/direction?destination=" + _latitude + "," + _longitude));
                            startActivity(intent);
                        }
                    });
                    dialog.setNegativeButton("我再想想", null);
                    dialog.show();
                    return;
                }
                if(isInstallBaiduMap("com.autonavi.minimap")){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(x_map.this);
                    dialog.setCancelable(false);
                    dialog.setTitle("提示");
                    dialog.setMessage("即将离开电梯云网,打开\"高德地图\"");
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setData(Uri.parse("amapuri://route/plan/?dlat=" + _latitude + "&dlon=" + _longitude + "&dname=" + addr + "&dev=0&t=0"));
                            startActivity(intent);
                        }
                    });
                    dialog.setNegativeButton("我再想想", null);
                    dialog.show();
                    return;
                }
                else{
                    AlertDialog.Builder dialog = new AlertDialog.Builder(x_map.this);
                    dialog.setCancelable(false);
                    dialog.setTitle("提示");
                    dialog.setMessage("您的手机尚未安装地图软件,点击进入浏览器导航");
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            PositionUtil.Gps gps = PositionUtil.bd09_To_Gcj02(_latitude, _longitude);
                            intent.setData(Uri.parse("http://uri.amap.com/navigation?to="+ gps.getWgLon() + "," + gps.getWgLat() + "&mode=car&src=nyx_super"));
                            startActivity(intent);
                        }
                    });
                    dialog.setNegativeButton("我再想想", null);
                    dialog.show();
                }
                break;
            case R.id.btn_delay:
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
            default:
                break;
        }
    }


    private class Task1 extends AsyncTask<String, Integer, String> {

        private String Task1_Result;
        private String xorder_id;

        public Task1(String xorder_id){
            this.xorder_id = xorder_id;
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = Constant.baseUrl + "api/v1/order/x_2?xorder_id=" + xorder_id;
            Task1_Result = Util_HttpConnect.getGetResult(url, x_map.this);
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject json = new JSONObject(Task1_Result);
                if(json.getInt("code") != 0){
                    dialog_over();
                    return;
                }
                String obj = json.getString("obj");
                JSONObject json2 = new JSONObject(obj);
                //解析出电梯的经纬度 地址 型号
                _longitude = Double.parseDouble(json2.getString("jing"));
                _latitude = Double.parseDouble(json2.getString("wei"));
                addr = json2.getString("addr");
                elevatorModel = json2.getString("elevatorModel");

                mapView = findViewById(R.id.bmapView);
                baiduMap = mapView.getMap();

                LatLng center = new LatLng(_latitude, _longitude);

                //设置中心点
                MapStatus mapStatus = new MapStatus.Builder().target(center).zoom(16).build();
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
                baiduMap.setMapStatus(mapStatusUpdate);

                //设置电梯位置标记
                OverlayOptions opt1 = new TextOptions().text("电梯").fontSize(40).position(center);
                OverlayOptions opt2 = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_action_location))
                        .position(center);
                List<OverlayOptions> list = new ArrayList<>();
                list.add(opt1);
                list.add(opt2);
                baiduMap.addOverlays(list);

                locationClient = new LocationClient(getApplicationContext());
                locationClient.registerLocationListener(myListener);
                LocationClientOption option = new LocationClientOption();
                option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
                option.setCoorType("bd09ll");
                //只取一次
                option.setScanSpan(0);
                option.setOpenGps(true);
                locationClient.setLocOption(option);
                locationClient.start();

                tv_1 = findViewById(R.id.tv_1);
                tv_1.setText("电梯地址: " + addr + "\n电梯型号: " + elevatorModel);
            }
            catch (JSONException e){
                e.printStackTrace();
                dialog_over();
                return;
            }
        }
    }

    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            LatLng center = new LatLng(latitude, longitude);
            OverlayOptions opt1 = new TextOptions().text("您的位置").fontSize(40).position(center);
            OverlayOptions opt2 = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_action_location))
                    .position(center);
            List<OverlayOptions> list = new ArrayList<>();
            list.add(opt1);
            list.add(opt2);
            baiduMap.addOverlays(list);
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
                Intent intent = new Intent(x_map.this, Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(x_map.this, x.class);
        startActivity(intent);
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

    private class Task2 extends AsyncTask{

        private String xorder_id;
        private String Task2_Result;

        public Task2(String xorder_id){
            this.xorder_id = xorder_id;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/x_4";
            String params = "{\"xorder_id\":\"" + xorder_id + "\"}";
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
                Intent intent = new Intent(x_map.this, x_center.class);
                Bundle bundle = new Bundle();
                bundle.putString("xorder_id", xorder_id);
                intent.putExtras(bundle);
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
                dialog_over();
                return;
            }
        }
    }

    //延时
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
                Intent intent = new Intent(x_map.this, Order_Success.class);
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

    private boolean isInstallBaiduMap(String url){
        List<PackageInfo> packages = MyApplication.getContext().getPackageManager().getInstalledPackages(0);
        for(PackageInfo p : packages){
            if(p.packageName.equals(url)){
                return true;
            }
            else{
                continue;
            }
        }
        return false;
    }
}

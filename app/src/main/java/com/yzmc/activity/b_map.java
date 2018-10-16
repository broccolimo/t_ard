package com.yzmc.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.yzmc.model.OperationModel;
import com.yzmc.model.OrderModel;
import com.yzmc.service.OperationService;
import com.yzmc.service.OrderService;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.GetCookie;
import com.yzmc.util.MyApplication;
import com.yzmc.util.PositionUtil;
import com.yzmc.util.SetCookie;
import com.yzmc.util.StringCollection;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.baidu.mapapi.synchronization.SyncCoordinateConverter.CoordType.BD09LL;

//把百度的jar包放进项目之后 要对着jar包右键 add as library
public class b_map extends AppCompatActivity implements View.OnClickListener{
    //控件
    private MapView mapView;
    //控件所指的百度地图对象
    private BaiduMap baiduMap;
    //开始按钮
    private Button btn_order_start;
    //退单按钮
    private Button btn_order_cancel;
    //导航按钮
    private Button nav;
    //最上边显示工作内容的控件
    private TextView tv_1;

    //工单id
    private String order_id;
    //电梯经度
    private Double _longitude;
    //电梯纬度
    private Double _latitude;
    //手机经度
    private Double longitude;
    //手机纬度
    private Double latitude;
    //电梯地址
    private String addr;
    //电梯型号
    private String productID;
    private boolean isFirstResume = true;

    //百度地图api所需全局变量
    private LocationClient locationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(MyApplication.getContext());
        setContentView(R.layout.b_map);
        AllActivity.addActivity(this);
        btn_order_start = findViewById(R.id.btn_order_start);
        btn_order_start.setOnClickListener(this);
        btn_order_cancel = findViewById(R.id.btn_order_cancel);
        btn_order_cancel.setOnClickListener(this);
        nav = findViewById(R.id.nav);
        nav.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        if(isFirstResume){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        Toast.makeText(b_map.this,"自Android 6.0开始需要打开位置权限",Toast.LENGTH_SHORT).show();
                    }
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            0);
                }
            }
            isFirstResume = false;
            Intent intent = getIntent();
            order_id = intent.getStringExtra("order_id");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(new SetCookie())
                            .build();
                    Retrofit retrofit = new Retrofit.Builder().baseUrl(Constant.baseUrl)
                            .addConverterFactory(GsonConverterFactory.create()).client(client).build();
                    OrderService orderService = retrofit.create(OrderService.class);
                    Call<OrderModel> call = orderService.getOrderInfo(order_id);
                    call.enqueue(new Callback<OrderModel>() {
                        @Override
                        public void onResponse(Call<OrderModel> call, Response<OrderModel> response) {
                            if(response.body().code != 0){
                                dialog_over();
                                return;
                            }
                            _longitude = response.body().obj.jing;
                            _latitude = response.body().obj.wei;
                            addr = response.body().obj.addr;
                            productID = response.body().obj.productID;

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
                            tv_1.setText("电梯地址: " + addr + "\n电梯型号: " + productID);
                        }

                        @Override
                        public void onFailure(Call<OrderModel> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            }).start();
        }

        super.onResume();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Bundle bundle;
        switch(v.getId()){
            case R.id.btn_order_start:
                showProgressDialog();
                LatLng ele_pos = new LatLng(latitude, longitude);
                LatLng per_pos = new LatLng(_latitude, _longitude);
                Double distance = DistanceUtil.getDistance(ele_pos, per_pos);
                if(distance < 200){
                    closeProgressDialog();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("提示");
                    dialog.setMessage("您与电梯距离较远，不能开始");
                    dialog.setPositiveButton("确定", null);
                    dialog.setCancelable(false);
                    dialog.show();
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OkHttpClient client = new OkHttpClient.Builder()
                                .addInterceptor(new SetCookie()).build();
                        Retrofit retrofit = new Retrofit.Builder().baseUrl(Constant.baseUrl)
                                .addConverterFactory(GsonConverterFactory.create()).client(client)
                                .build();
                        OperationService operationService = retrofit.create(OperationService.class);
                        Call<OperationModel> call = operationService.startBOrder(order_id, new Date());
                        call.enqueue(new Callback<OperationModel>() {
                            @Override
                            public void onResponse(Call<OperationModel> call, Response<OperationModel> response) {
                                Intent intent = new Intent(b_map.this, b_center.class);
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
                break;
            case R.id.btn_order_cancel:
                intent = new Intent(b_map.this, Order_Cancel.class);
                bundle = new Bundle();
                bundle.putString("order_id", order_id);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.nav:
                if(isInstallBaiduMap("com.baidu.BaiduMap")){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(b_map.this);
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
                    AlertDialog.Builder dialog = new AlertDialog.Builder(b_map.this);
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
                    AlertDialog.Builder dialog = new AlertDialog.Builder(b_map.this);
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
            default:
                break;
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
                Intent intent = new Intent(b_map.this, Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(b_map.this, b.class);
        startActivity(intent);
    }


    //正在加载框
    private ProgressDialog progressDialog;
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(b_map.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        progressDialog.setMessage("正在加载...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    private void closeProgressDialog(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
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

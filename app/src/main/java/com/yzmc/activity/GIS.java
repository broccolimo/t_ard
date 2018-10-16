package com.yzmc.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.clusterutil.clustering.Cluster;
import com.baidu.mapapi.clusterutil.clustering.ClusterManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.MyApplication;
import com.yzmc.util.MyItem;

import java.util.ArrayList;
import java.util.List;

public class GIS extends AppCompatActivity implements View.OnClickListener{

    private MapView mapView;
    private BaiduMap baiduMap;
    private Button btn1;
    private Button btn2;
    //private Button btn3;
    private ImageView back;

    private ClusterManager clusterManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(MyApplication.getContext());
        setContentView(R.layout.gis);
        AllActivity.addActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init(){
        mapView = findViewById(R.id.map);
        baiduMap = mapView.getMap();
        pre();
        addMarkers();
        //marker();
        //cluster();

        btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(this);
        btn2 = findViewById(R.id.btn2);
        btn2.setOnClickListener(this);
        //btn3 = findViewById(R.id.btn3);
        //btn3.setOnClickListener(this);
        back = findViewById(R.id.back);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn1:
                baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.btn2:
                baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            /*case R.id.btn3:
                baiduMap.setMapType(BaiduMap.MAP_TYPE_NONE);
                break;*/
            case R.id.back:
                onBackPressed();
                break;
            default:
                break;
        }
    }

    private void pre(){
        clusterManager = new ClusterManager<MyItem>(this, baiduMap);
        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener() {
            @Override
            public boolean onClusterClick(Cluster cluster) {
                Toast.makeText(GIS.this,
                        "有" + cluster.getSize() + "个点", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        baiduMap.setOnMapStatusChangeListener(clusterManager);
    }
    private ProgressDialog progressDialog;

    private void showProgressDialog(){
        progressDialog = new ProgressDialog(GIS.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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

    private void marker(){
        LatLng point = new LatLng(30.262683, 119.851558);
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_action_location);
        OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
        baiduMap.addOverlay(option);
        baiduMap.addOverlay(option);
        MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(16).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        baiduMap.setMapStatus(mapStatusUpdate);


        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                LatLng ll = marker.getPosition();
                TextView textView = new TextView(GIS.this);
                textView.setWidth(700);
                textView.setBackgroundColor(getResources().getColor(R.color.white));
                textView.setText("电梯位置: 西子电梯产业园区3号楼1号梯\n使用单位: 西子电梯科技有限公司\n状态: 运行中");
                InfoWindow infoWindow = new InfoWindow(textView, ll, -120);
                baiduMap.showInfoWindow(infoWindow);
                return true;
            }
        });
    }

    private void cluster(){
        LatLng point = new LatLng(30.262683, 119.851558);
        List<MyItem> items = new ArrayList<>();
        items.add(new MyItem(point));
        clusterManager.addItems(items);
    }

    public void addMarkers() {
        // 添加Marker点
        LatLng llA = new LatLng(39.963175, 116.400244);
        LatLng llB = new LatLng(39.942821, 116.369199);
        LatLng llC = new LatLng(39.939723, 116.425541);
        LatLng llD = new LatLng(39.906965, 116.401394);
        LatLng llE = new LatLng(39.956965, 116.331394);
        LatLng llF = new LatLng(39.886965, 116.441394);
        LatLng llG = new LatLng(39.996965, 116.411394);

        List<MyItem> items = new ArrayList<MyItem>();
        items.add(new MyItem(llA));
        items.add(new MyItem(llB));
        items.add(new MyItem(llC));
        items.add(new MyItem(llD));
        items.add(new MyItem(llE));
        items.add(new MyItem(llF));
        items.add(new MyItem(llG));

        clusterManager.addItems(items);

    }
}

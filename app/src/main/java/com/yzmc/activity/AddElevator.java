package com.yzmc.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;
import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.MyApplication;
import com.yzmc.util.PositionUtil;
import com.yzmc.util.Util_HttpConnect;
import com.yzq.zxinglibrary.android.CaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddElevator extends AppCompatActivity {

    private ImageView back;
    private Button btn_scan_1;
    private Button btn_scan_2;
    private TextView uid_1;
    private TextView uid_2;
    private Spinner zone;
    private Spinner province;
    private Spinner city;
    private Spinner area;
    private EditText et_time;
    private TextView tv_jing;
    private TextView tv_wei;
    private Context context = this;
    private LayoutInflater inflater;
    private TextView tv_1;
    private TextView tv_2;
    private TextView tv_3;
    private TextView tv_4;
    private TextView tv_5;
    private TextView tv_6;
    private TextView tv_7;
    private TextView tv_8;
    private TextView tv_9;
    private TextView tv_10;
    private TextView tv_11;
    private LinearLayout ll_1;
    private LinearLayout ll_2;
    private Button btn_add_a;
    private Button btn_del_a;
    //确定按钮
    private Button btn_commit;
    //获取经纬度
    private Button btn_position;
    //电梯详细地址
    private EditText et_addr;
    //安装单位
    private EditText et_installUnit;
    //电梯型号
    private Spinner sp_elevatorModel;
    //层
    private EditText conf_12;
    //站
    private EditText conf_13;
    //门
    private EditText conf_14;
    //曳引机型号
    private Spinner conf_1;
    //曳引机根数
    private Spinner conf_2;
    //曳引机直径
    private Spinner conf_3;
    //电机功率
    private EditText conf_4;
    //控制柜型号
    private Spinner conf_5;
    //制动器型号
    private Spinner conf_6;
    //限速器型号
    private Spinner conf_7;
    //安全钳型号
    private Spinner conf_8;
    //门锁型号
    private Spinner conf_9;
    //额定速度
    private Spinner conf_10;
    //额定载重
    private Spinner conf_11;
    //使用管理责任单位
    private EditText et_useUnit;
    //使用单位设备编号
    private EditText et_useUnitID;
    //电梯注册代码
    private EditText et_elevatorCode;
    //制造单位
    private EditText et_makeUnit;
    //维保单位
    private EditText et_maintainUnit;
    //手机经度
    private Double longitude;
    //手机纬度
    private Double latitude;
    private LocationClient locationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    private static int a_count = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_elevator);
        AllActivity.addActivity(this);
        init();
        inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);

        btn_scan_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddElevator.this, CaptureActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        btn_scan_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddElevator.this, CaptureActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        //南区/北区 给省添加下拉框数据
        zone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    province.setVisibility(View.GONE);
                    city.setVisibility(View.GONE);
                    area.setVisibility(View.GONE);
                    return;
                }
                province.setVisibility(View.VISIBLE);
                String[] items = getResources().getStringArray(getResources().getIdentifier("z" + position, "array", "com.yzmc"));
                province.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //省 给市添加下拉框数据
        province.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                city.setVisibility(View.VISIBLE);
                int z = zone.getSelectedItemPosition();
                String[] items = getResources().getStringArray(getResources().getIdentifier("z" + z + "_" + position, "array", "com.yzmc"));
                city.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //市 给区添加下拉框数据
        city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                area.setVisibility(View.VISIBLE);
                int z = zone.getSelectedItemPosition();
                int p = province.getSelectedItemPosition();
                String[] items = getResources().getStringArray(getResources().getIdentifier("z" + z + "_" + p + "_" + position, "array", "com.yzmc"));
                area.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        et_time.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showDatePickDlg();
                    return true;
                }
                return false;
            }
        });

        //一开始 安装人员就应该有一项
        View view = inflater.inflate(R.layout.add_person, null);
        view.setId(a_count++);
        ll_1.addView(view);

        //安装人员 添加
        btn_add_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = inflater.inflate(R.layout.add_person, null);
                view.setId(a_count++);
                ll_1.addView(view);
            }
        });

        //安装人员 删除
        btn_del_a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(a_count == 1){
                    return;
                }
                ll_1.removeViewAt(--a_count);
            }
        });


        //确定按钮事件
        btn_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                store();
            }
        });

        //左上角返回
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddElevator.this, Main.class);
                startActivity(intent);
            }
        });

        btn_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setCancelable(false);
                dialog.setTitle("提示");
                dialog.setMessage("请确定当前位置和新建电梯位置保持一致");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        locationClient = new LocationClient(MyApplication.getContext());
                        locationClient.registerLocationListener(myListener);
                        LocationClientOption option = new LocationClientOption();
                        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
                        option.setCoorType("bd09ll");
                        option.setScanSpan(0);
                        option.setOpenGps(true);
                        locationClient.setLocOption(option);
                        locationClient.start();
                    }
                });
                dialog.setNegativeButton("我再想想", null);
                dialog.show();
            }
        });

    }

    private void init(){
        back = findViewById(R.id.back);
        btn_scan_1 = findViewById(R.id.btn_scan_1);
        btn_scan_2 = findViewById(R.id.btn_scan_2);
        uid_1 = findViewById(R.id.uid_1);
        uid_2 = findViewById(R.id.uid_2);
        et_time = findViewById(R.id.et_time);
        tv_jing = findViewById(R.id.jing);
        tv_wei = findViewById(R.id.wei);
        tv_1 = findViewById(R.id.tv_1);
        addRedStarHeader(tv_1);
        tv_2 = findViewById(R.id.tv_2);
        addRedStarHeader(tv_2);
        tv_3 = findViewById(R.id.tv_3);
        addRedStarHeader(tv_3);
        tv_4 = findViewById(R.id.tv_4);
        addRedStarHeader(tv_4);
        tv_5 = findViewById(R.id.tv_5);
        addRedStarHeader(tv_5);
        tv_6 = findViewById(R.id.tv_6);
        addRedStarHeader(tv_6);
        tv_7 = findViewById(R.id.tv_7);
        addRedStarHeader(tv_7);
        tv_8 = findViewById(R.id.tv_8);
        addRedStarHeader(tv_8);
        tv_9 = findViewById(R.id.tv_9);
        addRedStarHeader(tv_9);
        tv_10 = findViewById(R.id.tv_10);
        addRedStarHeader(tv_10);
        tv_11 = findViewById(R.id.tv_11);
        addRedStarHeader(tv_11);
        zone = findViewById(R.id.zone);
        province = findViewById(R.id.province);
        city = findViewById(R.id.city);
        area = findViewById(R.id.area);
        ll_1 = findViewById(R.id.ll_1);
        btn_add_a = findViewById(R.id.btn_add_a);
        btn_del_a = findViewById(R.id.btn_del_a);
        btn_commit = findViewById(R.id.btn_commit);
        btn_position = findViewById(R.id.btn_position);
        et_addr = findViewById(R.id.et_addr);
        et_installUnit = findViewById(R.id.et_installUnit);
        sp_elevatorModel = findViewById(R.id.sp_elevatorModel);
        conf_12 = findViewById(R.id.conf_12);
        conf_13 = findViewById(R.id.conf_13);
        conf_14 = findViewById(R.id.conf_14);
        conf_1 = findViewById(R.id.conf_1);
        conf_2 = findViewById(R.id.conf_2);
        conf_3 = findViewById(R.id.conf_3);
        conf_4 = findViewById(R.id.conf_4);
        conf_5 = findViewById(R.id.conf_5);
        conf_6 = findViewById(R.id.conf_6);
        conf_7 = findViewById(R.id.conf_7);
        conf_8 = findViewById(R.id.conf_8);
        conf_9 = findViewById(R.id.conf_9);
        conf_10 = findViewById(R.id.conf_10);
        conf_11 = findViewById(R.id.conf_11);
        et_useUnit = findViewById(R.id.et_useUnit);
        et_useUnitID = findViewById(R.id.et_useUnitID);
        et_elevatorCode = findViewById(R.id.et_elevatorCode);
        et_makeUnit = findViewById(R.id.et_makeUnit);
        et_maintainUnit = findViewById(R.id.et_maintainUnit);
    }

    //一些字段前边加*
    private void addRedStarHeader(TextView view){
        SpannableString ss = new SpannableString("*" + view.getText());
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.MidnightBlue)), 0, 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        view.setText(ss);
    }

    //扫描结果处理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            final String result = bundle.getString("codedContent");
            if(requestCode == 0){
                uid_1.setVisibility(View.VISIBLE);
                uid_1.setText(result);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String url = Constant.baseUrl + "api/v1/order/newDeviceCheck?gatewayID=" + result;
                        String http_res = Util_HttpConnect.getGetResult(url, context);
                        try {
                            JSONObject json = new JSONObject(http_res);
                            String code = json.getString("code");
                            if(Integer.parseInt(code) != 0){
                                Message msg = new Message();
                                msg.what = 9999;
                                mHandler.sendMessage(msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            if(requestCode == 1){
                uid_2.setVisibility(View.VISIBLE);
                uid_2.setText(result);
            }
        }
    }

    //显示日历
    private void showDatePickDlg(){
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(AddElevator.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String month = String.valueOf(monthOfYear);
                month = month.length() == 1 ? "0" + month : month;
                String day = String.valueOf(dayOfMonth);
                day = day.length() == 1 ? "0" + day : day;
                AddElevator.this.et_time.setText(year + "-" + month + "-" + day);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void store(){
        final String gatewayID_val = String.valueOf(uid_1.getText());
        if(gatewayID_val.equals("")){
            dialog_error("请扫描电梯网关ID");
            return;
        }

        final String cameraID_val = String.valueOf(uid_2.getText());
        if(cameraID_val.equals("")){
            dialog_error("请扫描摄像头ID");
            return;
        }

        final String area_val = String.valueOf(zone.getSelectedItem());
        final String province_val = String.valueOf(province.getSelectedItem());
        final String city_val = String.valueOf(city.getSelectedItem());
        final String district_val = String.valueOf(area.getSelectedItem());
        if(district_val == "null"){
            dialog_error("请选择电梯地址");
            return;
        }

        final String addr_val = String.valueOf(et_addr.getText());
        if(addr_val.equals("")){
            dialog_error("请输入电梯详细地址");
            return;
        }
        double _jing;
        double _wei;
        try{
            _jing = Double.parseDouble(tv_jing.getText().toString());
            _wei = Double.parseDouble(tv_wei.getText().toString());
        }
        catch (Exception e){
            dialog_error("请获取电梯的经纬度");
            return;
        }

        final double jing = _jing;
        final double wei = _wei;


        final String start_date_val = String.valueOf(et_time.getText());
        if(start_date_val.equals("")){
            dialog_error("请选择安装时间");
            return;
        }

        final String installUnit_val = String.valueOf(et_installUnit.getText());
        if(installUnit_val.equals("")){
            dialog_error("请输入安装单位名称");
            return;
        }

        final String elevatorModel_val = String.valueOf(sp_elevatorModel.getSelectedItem());
        if(elevatorModel_val.equals("--请选择--")){
            dialog_error("请选择电梯型号");
            return;
        }

        //层 校验 1)输入不是整数 2)输入是0或者负数 3)例如09则视为9
        String floor_val = String.valueOf(conf_12.getText());
        if(floor_val.equals("")){
            dialog_error("请输入 层 信息");
            return;
        }
        try{
            int floorInt = Integer.parseInt(floor_val);
            if(floorInt == 0){
                throw new Exception();
            }
            floor_val = String.valueOf(floorInt);
        }
        catch (Exception e){
            dialog_error("请输入正确的层信息");
            return;
        }
        final String _floor_val = floor_val;


        String station_val = String.valueOf(conf_13.getText());
        if(station_val.equals("")){
            dialog_error("请输入 站 信息");
            return;
        }
        try{
            int stationInt = Integer.parseInt(station_val);
            if(stationInt == 0){
                throw new Exception();
            }
            station_val = String.valueOf(stationInt);
        }
        catch (Exception e){
            dialog_error("请输入正确的站信息");
            return;
        }
        final String _station_val = station_val;


        String door_val = String.valueOf(conf_14.getText());
        if(door_val.equals("")){
            dialog_error("请输入 门 信息");
            return;
        }
        try{
            int doorInt = Integer.parseInt(door_val);
            if(doorInt == 0){
                throw new Exception();
            }
            door_val = String.valueOf(doorInt);
        }
        catch (Exception e){
            dialog_error("请输入正确的门信息");
            return;
        }
        final String _door_val = door_val;



        StringBuffer sb_installPer = new StringBuffer();
        StringBuffer sb_installTel = new StringBuffer();
        sb_installPer.append("[");
        sb_installTel.append("[");
        //安装人员
        for(int i = 0; i < a_count; i++){
            View view = ll_1.getChildAt(i);
            String name = String.valueOf(((EditText)(view.findViewById(R.id.a_name))).getText());
            String tel = String.valueOf(((EditText)(view.findViewById(R.id.a_phone))).getText());
            if(name.equals("")){
                dialog_error("请输入第" + (i + 1) + "位安装人员的姓名");
                return;
            }
            if(tel.equals("")){
                dialog_error("请输入第" + (i + 1) + "位安装人员的电话");
                return;
            }
            String regex = "^(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$";
            if(!tel.matches(regex)){
                dialog_error("第" + (i + 1) + "位安装人员的电话格式不正确");
                return;
            }
            if(i == 0){
                sb_installPer.append("\"" + name + "\"");
                sb_installTel.append("\"" + tel + "\"");
                continue;
            }
            sb_installPer.append(",\"" + name + "\"");
            sb_installTel.append(",\"" + tel + "\"");
        }
        sb_installPer.append("]");
        sb_installTel.append("]");


        final String installPer = sb_installPer.toString();
        final String installTel = sb_installTel.toString();

        final String tractionMachineModel_val = String.valueOf(conf_1.getSelectedItem());
        final String tractionMachine_num_val = String.valueOf(conf_2.getSelectedItem());
        final String tractionMachine_diameter_val = String.valueOf(conf_3.getSelectedItem());

        String _motorPower_val = String.valueOf(conf_4.getText());
        if(!_motorPower_val.equals("")){
            try{
                double motorPowerInt = Double.parseDouble(_motorPower_val);
                _motorPower_val = String.valueOf(motorPowerInt) + "kw";
            }
            catch (Exception e){
                dialog_error("电机功率格式不正确");
                return;
            }
        }
        final String motorPower_val = _motorPower_val;
        final String controlBox_val = String.valueOf(conf_5.getSelectedItem());
        final String brakes_val = String.valueOf(conf_6.getSelectedItem());
        final String speedLimiter_val = String.valueOf(conf_7.getSelectedItem());
        final String safetyGear_val = String.valueOf(conf_8.getSelectedItem());
        final String doorLock_val = String.valueOf(conf_9.getSelectedItem());
        final String ratedSpeed_val = String.valueOf(conf_10.getSelectedItem());
        final String ratedLoad_val = String.valueOf(conf_11.getSelectedItem());

        final String useUnit_val = String.valueOf(et_useUnit.getText());
        final String useUnitID_val = String.valueOf(et_useUnitID.getText());
        final String registerID_val = String.valueOf(et_elevatorCode.getText());
        final String makeUnit_val = String.valueOf(et_makeUnit.getText());
        final String maintainUnit_val = String.valueOf(et_maintainUnit.getText());

        if(!registerID_val.equals("") && !registerID_val.matches("^[0-9]{20}$")){
            dialog_error("电梯注册代码格式不正确");
            return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage("确定要新建这部电梯妈?提交后不可修改");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String url = Constant.baseUrl + "api/v1/order/addNewDevice";
                        String params = "{" +
                                "\"gatewayID\":" + "\"" + gatewayID_val + "\"," +
                                "\"cameraID\":" + "\"" + cameraID_val + "\"," +
                                "\"area\":" + "\"" + area_val + "\"," +
                                "\"province\":" + "\"" + province_val + "\"," +
                                "\"city\":" + "\"" + city_val + "\"," +
                                "\"district\":" + "\"" + district_val + "\"," +
                                "\"addr\":" + "\"" + addr_val + "\"," +
                                "\"start_date\":" + "\"" + start_date_val + "\"," +
                                "\"installUnit\":" + "\"" + installUnit_val + "\"," +
                                "\"useUnit\":" + "\"" + useUnit_val + "\"," +
                                "\"useUnitID\":" + "\"" + useUnitID_val + "\"," +
                                "\"registerID\":" + "\"" + registerID_val + "\"," +
                                "\"makeUnit\":" + "\"" + makeUnit_val + "\"," +
                                "\"maintainUnit\":" + "\"" + maintainUnit_val + "\"," +
                                "\"elevatorModel\":" + "\"" + elevatorModel_val + "\"," +
                                "\"tractionMachineModel\":" + "\"" + tractionMachineModel_val + "\"," +
                                "\"tractionMachine_num\":" + "\"" + tractionMachine_num_val + "\"," +
                                "\"tractionMachine_diameter\":" + "\"" + tractionMachine_diameter_val + "\"," +
                                "\"motorPower\":" + "\"" + motorPower_val + "\"," +
                                "\"controlBox\":" + "\"" + controlBox_val + "\"," +
                                "\"brakes\":" + "\"" + brakes_val + "\"," +
                                "\"speedLimiter\":" + "\"" + speedLimiter_val + "\"," +
                                "\"safetyGear\":" + "\"" + safetyGear_val + "\"," +
                                "\"doorLock\":" + "\"" + doorLock_val + "\"," +
                                "\"ratedSpeed\":" + "\"" + ratedSpeed_val + "\"," +
                                "\"ratedLoad\":" + "\"" + ratedLoad_val + "\"," +
                                "\"floor\":" + "\"" + _floor_val + "\"," +
                                "\"station\":" + "\"" + _station_val + "\"," +
                                "\"door\":" + "\"" + _door_val + "\"," +
                                "\"installPer\":" + installPer + "," +
                                "\"installTel\":" + installTel + "," +
                                "\"jing\":" + jing + "," +
                                "\"wei\":" + wei +
                                "}";
                        String http_res = Util_HttpConnect.getPostResult(url, params, context);
                        try {
                            JSONObject json = new JSONObject(http_res);
                            String code = json.getString("code");
                            if(Integer.parseInt(code) == 0){
                                Message msg = new Message();
                                msg.what = 9998;
                                mHandler.sendMessage(msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        dialog.setNegativeButton("我再想想", null);
        dialog.show();
    }

    private void dialog_error(String content){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage(content);
        dialog.setPositiveButton("确定", null);
        dialog.show();
    }

    private void dialog_back(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage("该设备已经被注册过");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(AddElevator.this, Main.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    private void dialog_addSuccess(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage("该电梯新建成功");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(AddElevator.this, Main.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 9999:
                    dialog_back();
                    break;
                case 9998:
                    dialog_addSuccess();
                    break;
                default:
                    break;
            }
        }
    };

    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            Log.d("jy", "before: " + location.getLongitude());
            Log.d("jy", "before: " + location.getLatitude());
            tv_jing.setText(String.valueOf(location.getLongitude()));
            tv_wei.setText(String.valueOf(location.getLatitude()));
        }
    }
}

package com.yzmc.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.Util_HttpConnect;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Extrainfo extends AppCompatActivity {

    private Context context = this;
    private LayoutInflater inflater;
    private ImageView back;
    private EditText et_artificial_cost;
    private EditText et_trip_cost;
    private EditText et_material_cost;
    private EditText et_extra_info;
    private TextView tv_rating_time;
    private TextView tv_actual_time;
    private TextView tv_accept_person;
    private LinearLayout ll;
    private Button btn_add;
    private Button btn_del;
    private Button btn_next;

    private String order_id;
    private String rating_time;
    private String accept_time;
    private String prefinish_time;
    private String accept_person;
    private int count = 0;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extrainfo);
        AllActivity.addActivity(this);
        inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        init();

    }

    @Override
    protected void onResume() {
        new PageTask().execute();
        super.onResume();
    }

    private class PageTask extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/getOrderInfo?order_id=" + order_id;
            String res = Util_HttpConnect.getGetResult(url, context);
            try {
                JSONObject json = new JSONObject(res);
                int code = json.getInt("code");
                if(code != 0){
                    dialog_over();
                    return null;
                }
                String obj = json.getString("obj");
                JSONObject obj_json = new JSONObject(obj);
                rating_time = obj_json.getString("rating_time");
                accept_time = obj_json.getString("accept_time");
                accept_time = accept_time.replace("T", " ").replace(".000Z", "");
                prefinish_time = obj_json.getString("prefinish_time");
                prefinish_time = prefinish_time.replace("T", " ").replace(".000Z", "");
                accept_person = obj_json.getString("accept_person");
            } catch (JSONException e) {
                e.printStackTrace();
                dialog_over();
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            tv_rating_time.setText(rating_time);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startDate;
            Date endDate;
            try {
                startDate = sdf.parse(accept_time);
                endDate = sdf.parse(prefinish_time);
            } catch (ParseException e) {
                e.printStackTrace();
                dialog_over();
                return;
            }
            long timestampsub = endDate.getTime() - startDate.getTime();
            StringBuffer sb = new StringBuffer();
            int days = (int)Math.floor(timestampsub / (1000 * 60 * 60 * 24));
            if(days != 0) {
                sb.append(days + "天");
            }
            long level1 = timestampsub % (1000 * 3600 * 24);
            int hours = (int)Math.floor(level1 / (1000 * 60 * 60));
            if(hours != 0) {
                sb.append(hours + "小时");
            }
            long level2 = level1 % (1000 * 60 * 60);
            int minutes = (int)Math.floor(level2 / (1000 * 60));
            if(minutes != 0) {
                sb.append(minutes + "分钟");
            }
            tv_actual_time.setText(sb.toString());
            tv_accept_person.setText(accept_person);
            btn_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(count == 3){
                        dialog_note("最多添加3条协助人员信息");
                        return;
                    }
                    View view = inflater.inflate(R.layout.add_assist_person, null);
                    view.setId(count++);
                    ll.addView(view);
                }
            });
            btn_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(count == 0){
                        return;
                    }
                    ll.removeViewAt(--count);
                }
            });
            btn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    next();
                }
            });
            et_artificial_cost = findViewById(R.id.et_artificial_cost);
            et_trip_cost = findViewById(R.id.et_trip_cost);
            et_material_cost = findViewById(R.id.et_material_cost);
        }
    }

    private void init(){
        Intent intent = getIntent();
        order_id = intent.getStringExtra("order_id");

        tv_rating_time = findViewById(R.id.tv_rating_time);
        tv_actual_time = findViewById(R.id.tv_actual_time);
        tv_accept_person = findViewById(R.id.tv_accept_person);
        ll = findViewById(R.id.ll);
        btn_add = findViewById(R.id.btn_add);
        btn_del = findViewById(R.id.btn_del);
        btn_next = findViewById(R.id.btn_next);
        et_extra_info = findViewById(R.id.et_extra_info);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    //意外错误
    private void dialog_over(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage("数据异常,请重新登录");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Extrainfo.this,Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    private void dialog_note(String content){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage(content);
        dialog.setPositiveButton("确定", null);
        dialog.show();
    }

    private void next(){
        String artificial_cost_val = String.valueOf(et_artificial_cost.getText());
        String trip_cost_val = String.valueOf(et_trip_cost.getText());
        String material_cost_val = String.valueOf(et_material_cost.getText());
        if(artificial_cost_val.equals("")){
            artificial_cost_val = String.valueOf(0);
        }
        else{
            try{
                Double artificial_cost_Double = Double.parseDouble(artificial_cost_val);
                if(artificial_cost_Double < 0){
                    throw new Exception();
                }
                artificial_cost_val = String.valueOf(artificial_cost_Double);
            }
            catch (Exception e){
                dialog_note("请输入正确的人工费用");
                return;
            }
        }

        if(trip_cost_val.equals("")){
            trip_cost_val = String.valueOf(0);
        }
        else{
            try{
                Double trip_cost_Double = Double.parseDouble(trip_cost_val);
                if(trip_cost_Double < 0){
                    throw new Exception();
                }
                trip_cost_val = String.valueOf(trip_cost_Double);
            }
            catch (Exception e){
                dialog_note("请输入正确的出差费用");
                return;
            }
        }

        if(material_cost_val.equals("")){
            material_cost_val = String.valueOf(0);
        }
        else{
            try{
                Double material_cost_Double = Double.parseDouble(material_cost_val);
                if(material_cost_Double < 0){
                    throw new Exception();
                }
                material_cost_val = String.valueOf(material_cost_Double);
            }
            catch (Exception e){
                dialog_note("请输入正确的材料费用");
                return;
            }
        }

        final String _artificial_cost_val = artificial_cost_val;
        final String _trip_cost_val = trip_cost_val;
        final String _material_cost_val = material_cost_val;
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < count; i++){
            View view = ll.getChildAt(i);
            String name = String.valueOf(((EditText)(view.findViewById(R.id.ast_name))).getText());
            String tel = String.valueOf(((EditText)(view.findViewById(R.id.ast_phone))).getText());
            String company = String.valueOf(((EditText)(view.findViewById(R.id.ast_company))).getText());
            if(company.equals("")){
                company = "null";
            }
            if(name.equals("")){
                dialog_note("请输入第" + (i + 1) + "位协助人员的姓名");
                return;
            }
            if(tel.equals("")){
                dialog_note("请输入第" + (i + 1) + "位协助人员的电话");
                return;
            }
            String regex = "^(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$";
            if(!tel.matches(regex)){
                dialog_note("第" + (i + 1) + "位协助人员的电话格式不正确");
                return;
            }
            if(i != 0){
                sb.append(",");
            }
            sb.append(name + "|" + tel + "|" + company);
        }

        final String note_information = String.valueOf(et_extra_info.getText());
        final String assist = sb.toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = Constant.baseUrl + "api/v1/order/Opt_6";
                String params = "{\"order_id\":\"" + order_id + "\",\"artificial_cost\":\"" +
                        _artificial_cost_val + "\",\"trip_cost\":\"" +
                        _trip_cost_val + "\",\"material_cost\":\"" +
                        _material_cost_val + "\",\"note_information\":\"" +
                        note_information + "\",\"assist\":\"" +
                        assist + "\"}";
                Log.d("zzz", params);
                String res = Util_HttpConnect.getPostResult(url, params, context);
                Log.d("zzz", res);
                try {
                    JSONObject json = new JSONObject(res);
                    int code = json.getInt("code");
                    if(code != 0){
                        dialog_over();
                        return;
                    }
                    Intent intent = new Intent(Extrainfo.this, Feedback.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("order_id", order_id);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                    dialog_over();
                    return;
                }

            }
        }).start();
    }


}

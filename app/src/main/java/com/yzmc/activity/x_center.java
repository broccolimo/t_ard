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
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.StringCollection;
import com.yzmc.util.Util_HttpConnect;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class x_center extends AppCompatActivity {

    private ImageView back;
    private Button btn_delay;
    private TextView tv_useUnit;
    private TextView tv_addr;
    private TextView tv_elevatorModel;
    private TextView tv_ratedSpeed;
    private TextView tv_makeUnit;
    private TextView tv_accept_person;
    private TextView tv_ratedLoad;
    private TextView tv_layers;
    private RadioButton rb_yes;
    private RadioButton rb_no;
    private EditText et_faultCause;
    private EditText et_dealResult;
    private EditText et_replacement;
    private TextView tv_release_time;
    private EditText et_start_time;
    private EditText et_arrive_time;
    private EditText et_repair_time;
    private EditText et_chargeItem;
    private EditText et_repair_cost;
    private EditText et_fittings_cost;
    private Button btn_next;

    private Context context = this;
    private String xorder_id;
    private ProgressDialog progressDialog;

    private int isGuarantee = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.x_center);
        AllActivity.addActivity(this);
        init();
        give();
    }

    @Override
    protected void onResume() {
        new Task1(xorder_id).execute();
        super.onResume();
    }

    private void init(){
        back = findViewById(R.id.back);
        btn_delay = findViewById(R.id.btn_delay);
        tv_useUnit = findViewById(R.id.tv_useUnit);
        tv_addr = findViewById(R.id.tv_addr);
        tv_elevatorModel = findViewById(R.id.tv_elevatorModel);
        tv_ratedSpeed = findViewById(R.id.tv_ratedSpeed);
        tv_makeUnit = findViewById(R.id.tv_makeUnit);
        tv_accept_person = findViewById(R.id.tv_accept_person);
        tv_ratedLoad = findViewById(R.id.tv_ratedLoad);
        tv_layers = findViewById(R.id.tv_layers);
        rb_yes = findViewById(R.id.rb_yes);
        rb_no = findViewById(R.id.rb_no);
        et_faultCause = findViewById(R.id.et_faultCause);
        et_dealResult = findViewById(R.id.et_dealResult);
        et_replacement = findViewById(R.id.et_replacement);
        tv_release_time = findViewById(R.id.tv_release_time);
        et_start_time = findViewById(R.id.et_start_time);
        et_arrive_time = findViewById(R.id.et_arrive_time);
        et_repair_time = findViewById(R.id.et_repair_time);
        et_chargeItem = findViewById(R.id.et_chargeItem);
        et_repair_cost = findViewById(R.id.et_repair_cost);
        et_fittings_cost = findViewById(R.id.et_fittings_cost);
        btn_next = findViewById(R.id.btn_next);

        xorder_id = getIntent().getStringExtra("xorder_id");
    }

    private void give(){
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(x_center.this, x_center_next.class);
                startActivity(intent);
            }
        });

        rb_yes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isGuarantee = 2;
            }
        });

        rb_no.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isGuarantee = 1;
            }
        });

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });

        btn_delay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setCancelable(false);
                dialog.setTitle("提示");
                dialog.setMessage("确定要延时吗?提交后不可修改");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Task2(xorder_id).execute();
                    }
                });
                dialog.setNegativeButton("我再想想", null);
                dialog.show();
            }
        });

    }

    private void next(){
        if(isGuarantee == -1){
            dialog_content("请选择是否保修");
            return;
        }
        final String faultCause = String.valueOf(et_faultCause.getText()).trim();
        if(faultCause.equals("")){
            dialog_content("请输入故障原因");
            return;
        }
        final String dealResult = String.valueOf(et_dealResult.getText()).trim();
        if(dealResult.equals("")){
            dialog_content("请输入处理结果");
            return;
        }
        final String replacement = String.valueOf(et_replacement.getText()).trim();
        if(replacement.equals("")){
            dialog_content(("请输入更换配件"));
            return;
        }


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //false 防止宽松式转换日期 默认为true 如6月31日可被接受 转化为7月1日 实际上是不允许的
        sdf.setLenient(false);

        //出发时间
        String start_time = String.valueOf(et_start_time.getText()).trim();
        Date date_start_time = null;
        try {
            date_start_time = sdf.parse(start_time);
        } catch (ParseException e) {
            e.printStackTrace();
            dialog_content("请输入正确格式的出发时间");
            return;
        }
        final Date _date_start_time = date_start_time;

        //到达时间
        String arrive_time = String.valueOf(et_arrive_time.getText()).trim();
        Date date_arrive_time = null;
        try {
            date_arrive_time = sdf.parse(arrive_time);
        } catch (ParseException e) {
            e.printStackTrace();
            dialog_content("请输入正确格式的到达时间");
            return;
        }
        final Date _date_arrive_time = date_arrive_time;

        //修理时间
        String repair_time = String.valueOf(et_repair_time.getText()).trim();
        Date date_repair_time = null;
        try {
            date_repair_time = sdf.parse(repair_time);
        } catch (ParseException e) {
            e.printStackTrace();
            dialog_content("请输入正确格式的修理时间");
            return;
        }
        final Date _date_repair_time = date_repair_time;

        //收费项目
        final String chargeItem = String.valueOf(et_chargeItem.getText()).trim();
        if(chargeItem.equals("")){
            dialog_content(("请输入收费项目"));
            return;
        }

        //修理费
        final String repair_cost = String.valueOf(et_repair_cost.getText()).trim();
        try{
            Double.parseDouble(repair_cost);
        }
        catch (Exception e){
            e.printStackTrace();
            dialog_content("请输入正确格式的修理费");
            return;
        }

        //配件费
        final String fittings_cost = String.valueOf(et_fittings_cost.getText()).trim();
        try{
            Double.parseDouble(fittings_cost);
        }
        catch (Exception e){
            e.printStackTrace();
            dialog_content("请输入正确格式的配件费");
            return;
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = Constant.baseUrl + "api/v1/order/x_8";
                String params = "{\"xorder_id\":\"" + xorder_id +
                        "\",\"isGuarantee\":" + isGuarantee +
                        ",\"faultCause\":\"" + faultCause +
                        "\",\"dealResult\":\"" + dealResult +
                        "\",\"replacement\":\"" + replacement +
                        "\",\"start_time\":\"" + _date_start_time +
                        "\",\"arrive_time\":\"" + _date_arrive_time +
                        "\",\"repair_time\":\"" + _date_repair_time +
                        "\",\"chargeItem\":\"" + chargeItem +
                        "\",\"repair_cost\":\"" + repair_cost +
                        "\",\"fittings_cost\":\"" + fittings_cost +
                        "\"}";
                Log.d("zzz", params);
                String result = Util_HttpConnect.getPostResult(url, params, context);
                Log.d("zzz", result);
                try {
                    JSONObject json = new JSONObject(result);
                    if(json.optInt("code") != 0){
                        Message msg = new Message();
                        msg.what = -1;
                        h1.sendMessage(msg);
                        return;
                    }
                    Message msg = new Message();
                    msg.what = 0;
                    h1.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                    dialog_over();
                    return;
                }
            }
        }).start();
    }

    private void save(){
/*        showProgressDialog();
        final String faultCause = String.valueOf(et_faultCause.getText()).trim();
        final String dealResult = String.valueOf(et_dealResult.getText()).trim();
        final String replacement = String.valueOf(et_replacement.getText()).trim();



        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //false 防止宽松式转换日期 默认为true 如6月31日可被接受 转化为7月1日 实际上是不允许的
        sdf.setLenient(false);

        //出发时间
        String start_time = String.valueOf(et_start_time.getText()).trim();
        Date date_start_time = null;
        if(!start_time.equals("")){
            try {
                date_start_time = sdf.parse(start_time);
            } catch (ParseException e) {
                e.printStackTrace();
                dialog_content("请输入正确格式的出发时间");
                return;
            }
        }
        final Date _date_start_time = date_start_time;

        //到达时间
        String arrive_time = String.valueOf(et_arrive_time.getText()).trim();
        Date date_arrive_time = null;
        if(!arrive_time.equals("")){
            try {
                date_arrive_time = sdf.parse(arrive_time);
            } catch (ParseException e) {
                e.printStackTrace();
                dialog_content("请输入正确格式的到达时间");
                return;
            }
        }
        final Date _date_arrive_time = date_arrive_time;

        //修理时间
        String repair_time = String.valueOf(et_repair_time.getText()).trim();
        Date date_repair_time = null;
        if(!repair_time.equals("")){
            try {
                date_repair_time = sdf.parse(repair_time);
            } catch (ParseException e) {
                e.printStackTrace();
                dialog_content("请输入正确格式的修理时间");
                return;
            }
        }
        final Date _date_repair_time = date_repair_time;

        //收费项目
        final String chargeItem = String.valueOf(et_chargeItem.getText()).trim();

        //修理费
        final String repair_cost = String.valueOf(et_repair_cost.getText()).trim();
        if(!repair_cost.equals("")){
            try{
                Double.parseDouble(repair_cost);
            }
            catch (Exception e){
                e.printStackTrace();
                dialog_content("请输入正确格式的修理费");
                return;
            }
        }


        //配件费
        final String fittings_cost = String.valueOf(et_fittings_cost.getText()).trim();
        if(!fittings_cost.equals("")){
            try{
                Double.parseDouble(fittings_cost);
            }
            catch (Exception e){
                e.printStackTrace();
                dialog_content("请输入正确格式的配件费");
                return;
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = Constant.baseUrl + "api/v1/order/x_9";
                String params = "{\"xorder_id\":\"" + xorder_id +
                        "\",\"isGuarantee\":" + isGuarantee +
                        ",\"faultCause\":\"" + faultCause +
                        "\",\"dealResult\":\"" + dealResult +
                        "\",\"replacement\":\"" + replacement +
                        "\",\"start_time\":\"" + _date_start_time +
                        "\",\"arrive_time\":\"" + _date_arrive_time +
                        "\",\"repair_time\":\"" + _date_repair_time +
                        "\",\"chargeItem\":\"" + chargeItem +
                        "\",\"repair_cost\":\"" + repair_cost +
                        "\",\"fittings_cost\":\"" + fittings_cost +
                        "\"}";
                String result = Util_HttpConnect.getPostResult(url, params, context);
                Log.d("zzz", params);
                Log.d("zzz", result);
                try {
                    JSONObject json = new JSONObject(result);
                    if(json.optInt("code") != 0){
                        Message msg = new Message();
                        msg.what = -1;
                        h1.sendMessage(msg);
                        return;
                    }
                    Message msg = new Message();
                    msg.what = 0;
                    h1.sendMessage(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                    dialog_over();
                    return;
                }
            }
        }).start();*/
    }

    private class Task1 extends AsyncTask{

        private String xorder_id;
        private String Task1_Result;

        public Task1(String xorder_id){
            this.xorder_id = xorder_id;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/x_2?xorder_id=" + xorder_id;
            Task1_Result = Util_HttpConnect.getGetResult(url, context);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                JSONObject json = new JSONObject(Task1_Result);
                if(json.optInt("code") != 0){
                    closeProgressDialog();
                    dialog_over();
                    return;
                }
                String obj = json.optString("obj");
                Log.d("zzz", obj);
                JSONObject jobj = new JSONObject(obj);
                tv_useUnit.setText(jobj.optString("useUnit"));
                tv_addr.setText(jobj.optString("addr"));
                tv_elevatorModel.setText(jobj.optString("elevatorModel"));
                tv_ratedSpeed.setText(jobj.optString("ratedSpeed"));
                tv_makeUnit.setText(jobj.optString("makeUnit"));
                tv_accept_person.setText(jobj.optString("accept_person"));
                tv_ratedLoad.setText(jobj.optString("ratedLoad"));
                tv_layers.setText(jobj.optString("layers"));
                tv_release_time.setText(fromISODate(jobj.optString("release_time")));
                if(jobj.optInt("isGuarantee") == 1){
                    rb_no.setChecked(true);
                }
                if(jobj.optInt("isGuarantee") == 2){
                    rb_yes.setChecked(true);
                }
                if(jobj.optString("faultCause") != "null"){
                    et_faultCause.setText(jobj.optString("faultCause"));
                }
                if(jobj.optString("dealResult") != "null"){
                    et_dealResult.setText(jobj.optString("dealResult"));
                }
                if(jobj.optString("replacement") != "null"){
                    et_replacement.setText(jobj.optString("replacement"));
                }
                if(jobj.optString("chargeItem") != "null"){
                    et_chargeItem.setText(jobj.optString("chargeItem"));
                }
                if(jobj.optString("repair_cost") != "null"){
                    et_repair_cost.setText(jobj.optString("repair_cost"));
                }
                if(jobj.optString("fittings_cost") != "null"){
                    et_fittings_cost.setText(jobj.optString("fittings_cost"));
                }
                if(jobj.optString("start_time") != "null"){
                    et_start_time.setText(fromISODate(jobj.optString("start_time")));
                }
                if(jobj.optString("arrive_time") != "null"){
                    et_arrive_time.setText(fromISODate(jobj.optString("arrive_time")));
                }
                if(jobj.optString("repair_time") != "null"){
                    et_repair_time.setText(fromISODate(jobj.optString("repair_time")));
                }
                closeProgressDialog();
            } catch (Exception e) {
                e.printStackTrace();
                dialog_over();
                return;
            }
        }
    }

    private class Task2 extends AsyncTask{
        private String xorder_id;
        private String r;

        public Task2(String xorder_id){
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
                Intent intent = new Intent(x_center.this, Order_Success.class);
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

    private void dialog_over(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage(StringCollection.STR001);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(x_center.this, Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    private void dialog_content(String content){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage(content);
        dialog.setPositiveButton("确定", null);
        dialog.show();
    }

    private String fromISODate(String time) throws ParseException {
        if(!time.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z")){
            return "";
        }
        time=time.replaceFirst("T", " ").replaceFirst(".\\d{3}Z", "");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
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

    private Handler h1 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what != 0){
                closeProgressDialog();
                dialog_over();
                return;
            }
            Intent intent = new Intent(x_center.this, x_center_next.class);
            Bundle bundle = new Bundle();
            bundle.putString("xorder_id", xorder_id);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(x_center.this, x.class);
        startActivity(intent);
    }
}

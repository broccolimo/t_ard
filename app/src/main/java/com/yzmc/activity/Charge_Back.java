package com.yzmc.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.PersonItem;
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

public class Charge_Back extends AppCompatActivity {


    private Context context = this;
    private String result;
    private ListView lv;
    private ImageView back;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.charge_back);
        AllActivity.addActivity(this);
        init();
        give();
    }


    private void init(){
        lv = findViewById(R.id.lv);
        back = findViewById(R.id.back);
    }

    private void give(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        showProgressDialog();
        new PageTask().execute();
        super.onResume();
    }

    private class PageTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/findAllOrder?flag=7";
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
                dialog_over();
                return;
            }
            loadChargeList();
            closeProgressDialog();
        }
    }

    private void loadChargeList(){
        lv.removeAllViewsInLayout();
        List<Map<String, Object>> list = getData();
        if(list.size() == 0 || list == null){
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
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.item1,
                new String[]{"order_id", "useUnit", "addr", "registerID", "productID",
                        "release_time", "maintain_type"},
                new int[]{R.id.order_id, R.id.item_tv_1, R.id.item_tv_2,
                        R.id.item_tv_3, R.id.item_tv_4, R.id.item_tv_5, R.id.item_tv_6});
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Map<String, Object> map = (Map<String, Object>) parent.getItemAtPosition(position);
                String order_id = String.valueOf(map.get("order_id"));
                new Task1(order_id).execute();

            }
        });
    }

    private List<Map<String, Object>> getData(){
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("obj");
            for(int i = 0; i < jsonArray.length(); i++){
                String str = String.valueOf(jsonArray.get(i));
                JSONObject json = new JSONObject(str);
                Map<String, Object> map = new HashMap<>();
                //order_id
                map.put("order_id", json.optString("order_id"));
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
            dialog_over();
            return null;
        }

        return list;
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
                Intent intent = new Intent(Charge_Back.this, Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    private class Task1 extends AsyncTask{
        private String order_id;
        private String task_result;

        public Task1(String order_id){
            this.order_id = order_id;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/Opt_17?order_id=" + order_id;
            task_result = Util_HttpConnect.getGetResult(url, context);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                JSONObject json = new JSONObject(task_result);
                if(json.optInt("code") != 0){
                    dialog_over();
                    return;
                }
                String obj = json.optString("obj");
                JSONObject jobj = new JSONObject(obj);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("提示");
                builder.setCancelable(false);
                final AlertDialog dialog = builder.create();

                View view = LayoutInflater.from(context).inflate(R.layout.reassign, null);
                TextView cancel_time = view.findViewById(R.id.cancel_time);
                cancel_time.setText(fromISODate(jobj.optString("cancel_time")));
                TextView cancel_reason = view.findViewById(R.id.cancel_reason);
                cancel_reason.setText(jobj.optString("cancel_reason"));
                ArrayList<PersonItem> list = new ArrayList();

                JSONArray _name = jobj.getJSONArray("name");
                JSONArray _account = jobj.getJSONArray("account");
                for(int i = 0; i < _name.length(); i++){
                    PersonItem personItem = new PersonItem(String.valueOf(_name.get(i)), String.valueOf(_account.get(i)));
                    list.add(personItem);
                }
                final Spinner spinner = view.findViewById(R.id.person);
                ArrayAdapter<PersonItem> adapter = new ArrayAdapter<PersonItem>(context, android.R.layout.simple_spinner_dropdown_item, list);
                spinner.setAdapter(adapter);


                Button btn_cancel = view.findViewById(R.id.btn_cancel);
                btn_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                Button btn_restart = view.findViewById(R.id.btn_restart);
                btn_restart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = ((PersonItem)(spinner.getSelectedItem())).getName();
                        String account = ((PersonItem)(spinner.getSelectedItem())).getAccount();
                        new Task2(name, account, order_id).execute();
                    }
                });
                dialog.setView(view);
                dialog.show();
                closeProgressDialog();
            } catch (Exception e) {
                e.printStackTrace();
                dialog_over();
                return;
            }

        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0){
                closeProgressDialog();
                Intent intent = new Intent(Charge_Back.this, Order_Success.class);
                Bundle bundle = new Bundle();;
                bundle.putString("text", "重新指派成功");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    };

    private class Task2 extends AsyncTask{
        private String name;
        private String account;
        private String order_id;
        private String Opt_19Result;


        public Task2(String name, String account, String order_id){
            this.name = name;
            this.account = account;
            this.order_id = order_id;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String url = Constant.baseUrl + "api/v1/order/Opt_19?account=" + account;
            Opt_19Result = Util_HttpConnect.getGetResult(url, context);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            JSONObject json = null;
            try {
                json = new JSONObject(Opt_19Result);
                if(json.optInt("code") != 0){
                    dialog_over();
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                dialog_over();
                return;
            }
            String canAccept_b = json.optString("canAccept_b");
            if(canAccept_b == "false"){
                AlertDialog.Builder dialog1 = new AlertDialog.Builder(context);
                dialog1.setCancelable(false);
                dialog1.setTitle("提示");
                dialog1.setMessage(name + "当前有订单正在进行，暂时不能接收");
                dialog1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeProgressDialog();
                    }
                });
                dialog1.show();
                return;
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setCancelable(false);
            dialog.setTitle("提示");
            dialog.setMessage("确定要指派给" + name + "吗?提交后不可修改");
            dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new Task3(name, account, order_id).execute();
                }
            });
            dialog.setNegativeButton("我再想想", null);
            dialog.show();
            closeProgressDialog();
        }
    }

    private class Task3 extends AsyncTask{
        private String name;
        private String account;
        private String order_id;
        private String _res;

        public Task3(String name, String account, String order_id){
            this.name = name;
            this.account = account;
            this.order_id = order_id;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String _url = Constant.baseUrl + "api/v1/order/Opt_18";
            String _params = "{\"order_id\":\"" + order_id + "\",\"accept_account\":\"" +
                    account + "\",\"accept_person\":\"" +
                    name + "\",\"time\":\"" +
                    new Date() + "\"}";
            Log.d("zzz", _params);
            _res = Util_HttpConnect.getPostResult(_url, _params, context);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            try {
                JSONObject _json = new JSONObject(_res);
                if(_json.optInt("code") != 0){
                    dialog_over();
                    return;
                }
                Message msg = new Message();
                msg.what = 0;
                mHandler.sendMessage(msg);
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
}

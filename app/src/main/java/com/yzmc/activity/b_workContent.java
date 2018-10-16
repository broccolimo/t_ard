package com.yzmc.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.StringCollection;
import com.yzmc.util.Util_HttpConnect;

import org.json.JSONException;
import org.json.JSONObject;

public class b_workContent extends AppCompatActivity implements View.OnClickListener {
    private String addr;
    private String productID;
    private String order_id;
    private int code;

    private TextView tv_workContent;
    private Button btn_continueWrite;
    private Button btn_order_delay;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workcontent);
        AllActivity.addActivity(this);
        tv_workContent = findViewById(R.id.tv_workContent);
        btn_continueWrite = findViewById(R.id.btn_continueWrite);
        btn_continueWrite.setOnClickListener(this);
        back = findViewById(R.id.back);
        back.setOnClickListener(this);
        btn_order_delay = findViewById(R.id.btn_order_delay);
        btn_order_delay.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        new pageTask().execute();
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Bundle bundle;
        switch (v.getId()){
            case R.id.btn_continueWrite:
                intent = new Intent(b_workContent.this, b_center.class);
                bundle = new Bundle();
                bundle.putString("order_id", order_id);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.back:
                intent = new Intent(b_workContent.this, b.class);
                startActivity(intent);
                break;
            case R.id.btn_order_delay:
                intent = new Intent(b_workContent.this, Delay_Write.class);
                bundle = new Bundle();
                bundle.putString("order_id", order_id);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    private class pageTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            Intent intent = getIntent();
            order_id = intent.getStringExtra("order_id");
            String url = Constant.baseUrl + "api/v1/order/Opt_2?order_id=" + order_id;
            String result = Util_HttpConnect.getGetResult(url, b_workContent.this);
            try {
                JSONObject json1 = new JSONObject(result);
                code = json1.getInt("code");
                if(code != 0){
                    return null;
                }
                String obj = json1.getString("obj");
                JSONObject json2 = new JSONObject(obj);
                addr = json2.getString("addr");
                productID = json2.getString("productID");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if(code != 0){
                dialog_over();
                return;
            }
            tv_workContent.setText("电梯地址: " + addr + "\n电梯型号: " + productID);
            order_id = getIntent().getStringExtra("order_id");
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
                Intent intent = new Intent(b_workContent.this, Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }
}

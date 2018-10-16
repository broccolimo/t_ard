package com.yzmc.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.StringCollection;
import com.yzmc.util.Util_HttpConnect;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Delay_Write extends AppCompatActivity {

    private Context context = this;

    private ImageView back;
    private EditText et_delay_reason;
    private Button btn_commit;

    private String delay_reason;
    private String order_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delay_write);
        AllActivity.addActivity(this);
        init();
        give();
    }

    private void init(){
        back = findViewById(R.id.back);
        et_delay_reason = findViewById(R.id.et_delay_reason);
        btn_commit = findViewById(R.id.btn_commit);

        Intent intent = getIntent();
        order_id = intent.getStringExtra("order_id");
    }

    private void give(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(String.valueOf(et_delay_reason.getText()).replace(" ", "").equals("")){
                    dialog_check();
                    return;
                }
                delay_reason = String.valueOf(et_delay_reason.getText());
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setCancelable(false);
                dialog.setTitle("提示");
                dialog.setMessage("确定要延时此单吗?提交后不可修改");
                dialog.setNegativeButton("我再想想", null);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String url = Constant.baseUrl + "api/v1/order/Opt_14";
                                String params = "{\"delay_time\":\"" + new Date() +
                                        "\",\"delay_description\":\"" + delay_reason +
                                        "\",\"account\":\"" + PreferenceManager.getDefaultSharedPreferences(context).getString("account", "") +
                                        "\",\"order_id\":\"" + order_id + "\"}";
                                String res = Util_HttpConnect.getPostResult(url, params, context);
                                try {
                                    JSONObject json = new JSONObject(res);
                                    int code = json.optInt("code");
                                    Message msg = new Message();
                                    msg.what = code;
                                    mHandler_1.sendMessage(msg);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message msg = new Message();
                                    msg.what = -1;
                                    mHandler_1.sendMessage(msg);
                                    return;
                                }
                            }
                        }).start();
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void dialog_check(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage("请填写延时原因");
        dialog.setPositiveButton("确定", null);
        dialog.show();
    }

    private Handler mHandler_1 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what != 0){
                dialog_over();
                return;
            }
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("canAccept_b", true).commit();
            Intent intent = new Intent(context, Order_Success.class);
            Bundle bundle = new Bundle();
            bundle.putString("text", "延时成功");
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };

    private void dialog_over(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage(StringCollection.STR001);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(context, Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }
}

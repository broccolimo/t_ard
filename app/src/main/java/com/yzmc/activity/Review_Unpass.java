package com.yzmc.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yzmc.R;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.StringCollection;
import com.yzmc.util.Util_HttpConnect;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Review_Unpass extends AppCompatActivity {

    private Button btn_commit;
    private EditText et_refuse_reason;
    private Context context = this;
    private String order_id;
    private String actual_check_account;
    private String actual_check_name;
    private String refuse_reason;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_unpass);
        AllActivity.addActivity(this);
        init();
        give();
    }

    private void init(){
        btn_commit = findViewById(R.id.btn_commit);
        et_refuse_reason = findViewById(R.id.et_refuse_reason);
        Intent intent = getIntent();
        order_id = intent.getStringExtra("order_id");
        actual_check_account = intent.getStringExtra("actual_check_account");
        actual_check_name = intent.getStringExtra("actual_check_name");
    }

    private void give(){
        btn_commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(String.valueOf(et_refuse_reason.getText()).replace(" ", "").equals("")){
                    dialog_check();
                    return;
                }
                refuse_reason = String.valueOf(et_refuse_reason.getText());
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setCancelable(false);
                dialog.setTitle("提示");
                dialog.setMessage("确定不予通过吗?提交后不可修改");
                dialog.setNegativeButton("我再想想", null);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String url = Constant.baseUrl + "api/v1/order/Opt_12";
                                String params = "{\"flag\":4,\"refuse_time\":\"" + new Date() +
                                        "\",\"refuse_reason\":\"" + refuse_reason +
                                        "\",\"actual_check_account\":\"" + actual_check_account +
                                        "\",\"actual_check_name\":\"" + actual_check_name +
                                        "\",\"order_id\":\"" + order_id + "\"}";
                                String res = Util_HttpConnect.getPostResult(url, params, context);
                                try {
                                    JSONObject json = new JSONObject(res);
                                    int code = json.optInt("code");
                                    Message msg = new Message();
                                    msg.what = code;
                                    mHandler.sendMessage(msg);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message msg = new Message();
                                    msg.what = -1;
                                    mHandler.sendMessage(msg);
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

    private void dialog_check(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage("请填写不予通过的理由");
        dialog.setPositiveButton("确定", null);
        dialog.show();
    }

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

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what != 0){
                dialog_over();
                return;
            }
            Intent intent = new Intent(context, Order_Success.class);
            Bundle bundle = new Bundle();
            bundle.putString("text", "审核成功");
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };
}

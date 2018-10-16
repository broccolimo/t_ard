package com.yzmc.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzmc.R;
import com.yzmc.activity.Login;
import com.yzmc.activity.Order_Success;
import com.yzmc.activity.Review;
import com.yzmc.activity.ReviewDetail;
import com.yzmc.activity.Review_Unpass;
import com.yzmc.util.Constant;
import com.yzmc.util.StringCollection;
import com.yzmc.util.Util_HttpConnect;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Fragment_review extends Fragment {

    private Context context;
    private String order_id;
    private String maintain_type;
    private String maintenceInfo;
    private JSONObject json;
    private View view;
    private Button btn_pass;
    private Button btn_unpass;
    private int item_num = 0;
    private SharedPreferences pref;
    private String actual_check_account;
    private String actual_check_name;
    private int needLoadPhotoNum = 0;
    private int circleNum = 0;


    @Override
    public void onAttach(Context context) {
        this.context = context;
        Bundle bundle = getArguments();
        order_id = bundle.getString("order_id");
        maintain_type = bundle.getString("maintain_type");
        maintenceInfo = bundle.getString("maintenceInfo");
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        switch (maintain_type){
            case "半月维保":
                item_num = 32;
                view = inflater.inflate(R.layout._m0, null);
                break;
            case "季度维保":
                item_num = 13;
                view = inflater.inflate(R.layout._m1, null);
                break;
            case "半年维保":
                item_num = 15;
                view = inflater.inflate(R.layout._m2, null);
                break;
            case "一年维保":
                item_num = 17;
                view = inflater.inflate(R.layout._m3, null);
                break;
        }
        init();
        give();
        return view;
    }


    private void init(){
        btn_pass = view.findViewById(R.id.btn_pass);
        btn_unpass = view.findViewById(R.id.btn_unpass);
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        actual_check_account = pref.getString("account", "");
        actual_check_name = pref.getString("name", "");
    }


    private void give(){
        try {
            json = new JSONObject(maintenceInfo);
            for(int i = 1; i <= item_num; i++){
                String meta = json.getString("num" + i);
                JSONObject jm = new JSONObject(meta);
                //status
                TextView status = view.findViewById(getResources().getIdentifier("num" + i + "_status_TextView", "id", "com.yzmc"));
                switch(Integer.parseInt(jm.getString("status"))){
                    case 1:
                        status.setText("正常");
                        break;
                    case 2:
                        status.setText("不正常");
                        break;
                    case 3:
                        status.setText("不存在");
                        break;
                }
                //result
                TextView result = view.findViewById(getResources().getIdentifier("num" + i + "_result_TextView", "id", "com.yzmc"));
                switch(Integer.parseInt(jm.getString("result"))){
                    case 1:
                        result.setText("正常");
                        break;
                    case 2:
                        result.setText("不正常");
                        break;
                    case 3:
                        result.setText("不存在");
                        break;
                    }
                TextView cb1 = view.findViewById(getResources().getIdentifier("num" + i + "_cb1", "id", "com.yzmc"));
                TextView cb2 = view.findViewById(getResources().getIdentifier("num" + i + "_cb2", "id", "com.yzmc"));
                TextView et1 = view.findViewById(getResources().getIdentifier("num" + i + "_et1", "id", "com.yzmc"));
                TextView et2 = view.findViewById(getResources().getIdentifier("num" + i + "_et2", "id", "com.yzmc"));
                if(jm.getString("cb1").equals("1")){
                    cb1.setVisibility(View.VISIBLE);
                    et1.setVisibility(View.VISIBLE);
                    et1.setText(jm.getString("tj"));
                }
                if(jm.getString("cb2").equals("1")){
                    cb2.setVisibility(View.VISIBLE);
                    et2.setVisibility(View.VISIBLE);
                    et2.setText(jm.getString("gh"));
                }

                if(!jm.getString("imagePath").equals("")){
                    needLoadPhotoNum++;
                    final String imagePath = jm.getString("imagePath");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String url = Constant.baseUrl + "api/v1/order/androidLoadPhoto?imagePath=" + imagePath;
                            String stream = Util_HttpConnect.getGetResult(url, context);
                            Message msg = new Message();
                            Map<String, String> map = new HashMap<>();
                            map.put("stream", stream);
                            map.put("imagePath", imagePath);
                            msg.obj = map;
                            mHandler.sendMessage(msg);
                        }
                    }).start();
                }
                circleNum++;
            }
        } catch (Exception e) {
            dialog_over();
            return;
        }

        btn_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setCancelable(false);
                dialog.setTitle("提示");
                dialog.setMessage("确定要通过此维保单吗？提交后不可更改");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String url = Constant.baseUrl + "api/v1/order/Opt_10";
                                String params = "{\"flag\":5,\"pass_time\":\"" + new Date() +
                                        "\",\"actual_check_account\":\"" + actual_check_account +
                                        "\",\"actual_check_name\":\"" + actual_check_name +
                                        "\",\"order_id\":\"" + order_id + "\"}";
                                String res = Util_HttpConnect.getPostResult(url, params, context);
                                try {
                                    JSONObject json = new JSONObject(res);
                                    int code = json.getInt("code");
                                    Message msg = new Message();
                                    msg.what = code;
                                    mHandler2.sendMessage(msg);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Message msg = new Message();
                                    msg.what = -1;
                                    mHandler2.sendMessage(msg);
                                }
                            }
                        }).start();
                    }
                });
                dialog.setNegativeButton("我再想想", null);
                dialog.show();
            }
        });

        btn_unpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Review_Unpass.class);
                Bundle bundle = new Bundle();
                bundle.putString("order_id", order_id);
                bundle.putString("actual_check_account", actual_check_account);
                bundle.putString("actual_check_name", actual_check_name);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }


    private Handler mHandler = new Handler(){
        private int num = 0;
        @Override
        public void handleMessage(Message msg) {
            try{
                num++;
                Map map = (Map)msg.obj;
                String stream = (String)map.get("stream");
                byte[] bitmapByte = Base64.decode(stream, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);
                String imagePath = (String)map.get("imagePath");
                ImageView imageView = view.findViewById(getResources().getIdentifier(imagePath.split("/")[3].split("\\.")[0] + "_img", "id", "com.yzmc"));
                imageView.setImageBitmap(bitmap);
            }catch(Exception e){
                e.printStackTrace();
                //这个真没办法处理
                //刚进来 你就关 线程还没跑完呢
            }
            if(num == needLoadPhotoNum && circleNum == item_num){
                closeProgressDialog();
            }
        }
    };

    private Handler mHandler2 = new Handler(){
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

    private void closeProgressDialog(){
        ReviewDetail.progressDialog.dismiss();
    }
}

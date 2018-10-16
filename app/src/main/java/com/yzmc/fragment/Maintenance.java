package com.yzmc.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.yzmc.R;
import com.yzmc.activity.Extrainfo;
import com.yzmc.activity.Login;
import com.yzmc.util.Constant;
import com.yzmc.util.StringCollection;
import com.yzmc.util.Util_HttpConnect;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Maintenance extends Fragment{

    private Context context;
    private MyListener2 listener;

    private String maintenceInfo;
    private String maintain_id;
    private String order_id;
    private int maintain_type;
    private JSONObject json;
    private FragmentActivity activity;

    private Set<Integer> numlist = new HashSet<>();
    private View view;
    private Button button_store;
    private Button btn_next;

    //维保项的数目
    private int item_num = 0;
    //保存时需要核对的数量 由于2个单选框/tj/gh肯定要核对 所以初始值为4
    //照片每成功拍照一次 此数值加1
    private int opt_num = 6;

    //status array
    private int[] status_arr = null;
    //result array
    private int[] result_arr = null;
    //tj array
    private String[] tj_arr = null;
    //gh array
    private String[] gh_arr = null;
    //cb1 array
    private int[] cb1_arr = null;
    //cb2 array
    private int[] cb2_arr = null;

    @Override
    public void onAttach(Context context) {
        this.context = context;
        Bundle bundle = getArguments();
        maintenceInfo = bundle.getString("maintenceInfo");
        maintain_id = bundle.getString("maintain_id");
        order_id = bundle.getString("order_id");
        maintain_type = bundle.getInt("maintain_type");

        switch (maintain_type){
            case 0:
                item_num = 32;
                break;
            case 1:
                item_num = 13;
                break;
            case 2:
                item_num = 15;
                break;
            case 3:
                item_num = 17;
                break;
        }
        status_arr = new int[item_num];
        result_arr = new int[item_num];
        tj_arr = new String[item_num];
        gh_arr = new String[item_num];
        cb1_arr = new int[item_num];
        cb2_arr = new int[item_num];

        listener = (MyListener2) getActivity();
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        switch (maintain_type){
            case 0:
                view = inflater.inflate(R.layout.m0, null);
                break;
            case 1:
                view = inflater.inflate(R.layout.m1, null);
                break;
            case 2:
                view = inflater.inflate(R.layout.m2, null);
                break;
            case 3:
                view = inflater.inflate(R.layout.m3, null);
                break;
        }
        activity = this.getActivity();

        give();
        basis();
        return view;
    }

    private void give(){
        try {
            json = new JSONObject(maintenceInfo);
            for(int i = 1; i <= item_num; i++){
                final int num = i;
                String meta = json.getString("num" + num);
                JSONObject jm = new JSONObject(meta);
                //处理现状下拉框
                Spinner status = view.findViewById(getResources().getIdentifier("num" + num + "_status_spinner", "id", "com.yzmc"));
                if(!jm.getString("status").equals("")){
                    status.setSelection(Integer.parseInt(jm.getString("status")));
                    status_arr[num - 1] = Integer.parseInt(jm.getString("status"));
                }
                //处理结果下拉框
                Spinner result = view.findViewById(getResources().getIdentifier("num" + num + "_result_spinner", "id", "com.yzmc"));
                if(!jm.getString("result").equals("")){
                    result.setSelection(Integer.parseInt(jm.getString("result")));
                    result_arr[num - 1] = Integer.parseInt(jm.getString("result"));
                }


                final CheckBox cb1 = view.findViewById(getResources().getIdentifier("num" + num + "_cb1", "id", "com.yzmc"));
                final CheckBox cb2 = view.findViewById(getResources().getIdentifier("num" + num + "_cb2", "id", "com.yzmc"));
                final EditText et1 = view.findViewById(getResources().getIdentifier("num" + num + "_et1", "id", "com.yzmc"));
                final EditText et2 = view.findViewById(getResources().getIdentifier("num" + num + "_et2", "id", "com.yzmc"));
                final Button button = view.findViewById(getResources().getIdentifier("num" + num + "_btn", "id", "com.yzmc"));

                //checkbox is or not checked
                if(jm.getString("cb1").equals("1")){
                    //2个单选框全显示
                    cb1.setVisibility(View.VISIBLE);
                    cb2.setVisibility(View.VISIBLE);
                    //且此单选框被选中
                    cb1.setChecked(true);
                    //无论后边有没有内容,都显示
                    et1.setVisibility(View.VISIBLE);
                    cb1_arr[num - 1] = 1;
                }

                if(jm.getString("cb2").equals("1")){
                    cb1.setVisibility(View.VISIBLE);
                    cb2.setVisibility(View.VISIBLE);
                    cb2.setChecked(true);
                    et2.setVisibility(View.VISIBLE);
                    cb2_arr[num - 1] = 1;
                }


                cb1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            cb1_arr[num - 1] = 1;
                            et1.setVisibility(View.VISIBLE);
                            return;
                        }
                        et1.setText("");
                        cb1_arr[num - 1] = 0;
                        et1.setVisibility(View.GONE);
                    }
                });
                cb2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            cb2_arr[num - 1] = 1;
                            et2.setVisibility(View.VISIBLE);
                            return;
                        }
                        et2.setText("");
                        cb2_arr[num - 1] = 0;
                        et2.setVisibility(View.GONE);
                    }
                });

                //Editview
                if(!jm.getString("tj").equals("")){
                    et1.setText(jm.getString("tj"));
                    et1.setVisibility(View.VISIBLE);
                }
                if(!jm.getString("gh").equals("")){
                    et2.setText(jm.getString("gh"));
                    et2.setVisibility(View.VISIBLE);
                }




                if(!jm.getString("imagePath").equals("")){
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

                //下拉列表 单选框 事件处理
                status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        status_arr[num - 1] = position;
                        String value = String.valueOf(parent.getItemAtPosition(position));
                        if(value.equals("不正常")){
                            cb1.setVisibility(View.VISIBLE);
                            cb2.setVisibility(View.VISIBLE);
                        }
                        else{
                            cb1.setChecked(false);
                            cb2.setChecked(false);
                            cb1.setVisibility(View.GONE);
                            cb2.setVisibility(View.GONE);
                            et1.setText("");
                            et2.setText("");
                            et1.setVisibility(View.GONE);
                            et2.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                result.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        result_arr[num - 1] = position;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                        File out = new File(getPhotopath());
                        Uri uri = Uri.fromFile(out);
                        // 获取拍照后未压缩的原图片，并保存在uri路径中
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(intent, num);
                    }
                });

                if(i == item_num) {
                    listener.sendMessage2(0);
                }
            }
        } catch (Exception e) {
            return;
        }

    }

    private void basis(){
        button_store = view.findViewById(R.id.btn_store);
        button_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button_store.setText("保存中...");
                button_store.setClickable(false);
                store(9998);
            }
        });
        btn_next = view.findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Map map = (Map)msg.obj;
            String stream = (String)map.get("stream");
            byte[] bitmapByte = Base64.decode(stream, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapByte, 0, bitmapByte.length);
            String imagePath = (String)map.get("imagePath");
            ImageView imageView = view.findViewById(getResources().getIdentifier(imagePath.split("/")[3].split("\\.")[0] + "_img", "id", "com.yzmc"));
            imageView.setImageBitmap(bitmap);
        }
    };

    private Handler mHandler_2 = new Handler(){
        private int count = 0;
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1 == 1){
                count++;
            }
            if(count == opt_num && msg.what == 9998){
                //保存后还原数值
                count = 0;
                opt_num = 6;
                dialog_store_success();
                button_store.setText("保存");
                button_store.setClickable(true);
            }
            if(count == opt_num && msg.what == 9999){
                count = 0;
                opt_num = 6;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String url = Constant.baseUrl + "api/v1/order/Opt_5";
                        String params = "{\"prefinish_time\":\"" + new Date() + "\",\"order_id\":\"" + order_id + "\"}";
                        String res = Util_HttpConnect.getPostResult(url, params, context);
                        int code = -1;
                        try {
                            JSONObject json = new JSONObject(res);
                            code = json.getInt("code");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(code == 0){
                            Intent intent = new Intent(activity, Extrainfo.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("order_id", order_id);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            return;
                        }
                        dialog_over();
                    }
                }).start();

            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != -1){
            return;
        }
        numlist.add(requestCode);
        opt_num++;
        Bitmap bitmap = getBitmapFromUrl(getPhotopath(), 1000, 1000);
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(90);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                true);
        ImageView imageView = view.findViewById(getResources().getIdentifier("num" + requestCode + "_img", "id", "com.yzmc"));
        imageView.setImageBitmap(bitmap);

    }

    //不要动
    private String getPhotopath() {
        String fileName = "";
        String pathUrl = Environment.getExternalStorageDirectory()+"/dtyw/";
        String imageName = "temp.jpg";
        File file = new File(pathUrl);
        if(!file.exists()){
            file.mkdirs();
        }
        fileName = pathUrl + imageName;
        return fileName;
    }

    //不要动
    private Bitmap getBitmapFromUrl(String url, double width, double height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(url);
        options.inJustDecodeBounds = false;
        int mWidth = bitmap.getWidth();
        int mHeight = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = 1;
        float scaleHeight = 1;
        if(mWidth <= mHeight) {
            scaleWidth = (float) (width/mWidth);
            scaleHeight = (float) (height/mHeight);
        } else {
            scaleWidth = (float) (height/mWidth);
            scaleHeight = (float) (width/mHeight);
        }
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, mWidth, mHeight, matrix, true);
        bitmap.recycle();
        return newBitmap;
    }

    private void store(final int storeOrNext){

        //把图片存了
        for(int i = 1; i <= item_num; i++){
            if(numlist.contains(i)){
                ImageView imageView = view.findViewById(getResources().getIdentifier("num" + i + "_img", "id", "com.yzmc"));
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] bitmapBytes = baos.toByteArray();
                final String res = Base64.encodeToString(bitmapBytes, Base64.NO_WRAP);
                final int _i = i;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //上传图片本体
                        String url = Constant.baseUrl + "api/v1/order/photoAndUpload";
                        String params = "{\"name\":\"" + maintain_id + "/num" + _i + "\",\"maintain_id\":\"" + maintain_id + "\",\"data\":\"" + res + "\"}";
                        Util_HttpConnect.getPostResult(url, params, context);
                        //存储图片路径
                        String _url = Constant.baseUrl + "api/v1/order/StorePhotoPath" + maintain_type;
                        String _params = "{\"maintain_id\":\"" + maintain_id + "\",\"item\":\"num" + _i + "\"}";
                        Util_HttpConnect.getPostResult(_url, _params, context);
                        Message msg = new Message();
                        msg.arg1 = 1;
                        msg.what = storeOrNext;
                        mHandler_2.sendMessage(msg);
                    }
                }).start();
            }
        }

        //num1 num2 num3 ...
        StringBuffer sb = new StringBuffer();
        for(int i = 1; i <= item_num; i++){
            sb.append("\"num" + i + "\",");
        }
        String item = sb.toString();
        final String _item = item.substring(0, item.length() - 1);

        //status int[]
        StringBuffer sb2 = new StringBuffer();
        for(int i = 1; i <= item_num; i++){
            sb2.append(status_arr[i - 1] + ",");
        }
        String value = sb2.toString();
        final String _value = value.substring(0, value.length() - 1);

        //result int[]
        StringBuffer sb3 = new StringBuffer();
        for(int i = 1; i <= item_num; i++){
            sb3.append(result_arr[i - 1] + ",");
        }
        String value_ = sb3.toString();
        final String _value_ = value_.substring(0, value_.length() - 1);

        //tj gh array give value
        for(int i = 1; i <= item_num; i++){
            EditText tj = view.findViewById(getResources().getIdentifier("num" + i + "_et1", "id", "com.yzmc"));
            tj_arr[i - 1] = String.valueOf(tj.getText());
            EditText gh = view.findViewById(getResources().getIdentifier("num" + i + "_et2", "id", "com.yzmc"));
            gh_arr[i - 1] = String.valueOf(gh.getText());
        }

        //tj String[]
        StringBuffer sb4 = new StringBuffer();
        for(int i = 1; i <= item_num; i++){
            sb4.append("\"" + tj_arr[i - 1] + "\",");
        }
        String tjv = sb4.toString();
        final String _tjv = tjv.substring(0, tjv.length() - 1);

        //gh String[]
        StringBuffer sb5 = new StringBuffer();
        for(int i = 1; i <= item_num; i++){
            sb5.append("\"" + gh_arr[i - 1] + "\",");
        }
        String ghv = sb5.toString();
        final String _ghv = ghv.substring(0, ghv.length() - 1);

        //cb1 int[]
        StringBuffer sb6 = new StringBuffer();
        for(int i = 1; i <= item_num; i++){
            sb6.append(cb1_arr[i - 1] + ",");
        }
        String cb1v = sb6.toString();
        final String _cb1v = cb1v.substring(0, cb1v.length() - 1);

        //cb2 int[]
        StringBuffer sb7 = new StringBuffer();
        for(int i = 1; i <= item_num; i++){
            sb7.append(cb2_arr[i - 1] + ",");
        }
        String cb2v = sb7.toString();
        final String _cb2v = cb2v.substring(0, cb2v.length() - 1);

        //store status thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = Constant.baseUrl + "api/v1/order/StoreStatus" + maintain_type;
                String params = "{\"item\":[" + _item + "],\"maintain_id\":\"" + maintain_id + "\",\"value\":[" + _value + "]}";
                Util_HttpConnect.getPostResult(url, params, context);
                Message msg = new Message();
                msg.arg1 = 1;
                msg.what = storeOrNext;
                mHandler_2.sendMessage(msg);
            }
        }).start();

        //store result thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = Constant.baseUrl + "api/v1/order/StoreResult" + maintain_type;
                String params = "{\"item\":[" + _item + "],\"maintain_id\":\"" + maintain_id + "\",\"value\":[" + _value_ + "]}";
                Util_HttpConnect.getPostResult(url, params, context);
                Message msg = new Message();
                msg.arg1 = 1;
                msg.what = storeOrNext;
                mHandler_2.sendMessage(msg);
            }
        }).start();

        //store tj thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = Constant.baseUrl + "api/v1/order/StoreTj" + maintain_type;
                String params = "{\"item\":[" + _item + "],\"maintain_id\":\"" + maintain_id + "\",\"value\":[" + _tjv + "]}";
                Util_HttpConnect.getPostResult(url, params, context);
                Message msg = new Message();
                msg.arg1 = 1;
                msg.what = storeOrNext;
                mHandler_2.sendMessage(msg);
            }
        }).start();

        //store gh thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = Constant.baseUrl + "api/v1/order/StoreGh" + maintain_type;
                String params = "{\"item\":[" + _item + "],\"maintain_id\":\"" + maintain_id + "\",\"value\":[" + _ghv + "]}";
                Util_HttpConnect.getPostResult(url, params, context);
                Message msg = new Message();
                msg.arg1 = 1;
                msg.what = storeOrNext;
                mHandler_2.sendMessage(msg);
            }
        }).start();

        //store cb1 thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = Constant.baseUrl + "api/v1/order/StoreCb1_" + maintain_type;
                String params = "{\"item\":[" + _item + "],\"maintain_id\":\"" + maintain_id + "\",\"value\":[" + _cb1v + "]}";
                Util_HttpConnect.getPostResult(url, params, context);
                Message msg = new Message();
                msg.arg1 = 1;
                msg.what = storeOrNext;
                mHandler_2.sendMessage(msg);
            }
        }).start();

        //store cb2 thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = Constant.baseUrl + "api/v1/order/StoreCb2_" + maintain_type;
                String params = "{\"item\":[" + _item + "],\"maintain_id\":\"" + maintain_id + "\",\"value\":[" + _cb2v + "]}";
                Util_HttpConnect.getPostResult(url, params, context);
                Message msg = new Message();
                msg.arg1 = 1;
                msg.what = storeOrNext;
                mHandler_2.sendMessage(msg);
            }
        }).start();


    }

    private void next(){
        if(!check()){
            dialog_check_false();
            return;
        }
        store(9999);

    }

    private boolean check(){
        for(int i = 1; i <= item_num; i++){
            if(result_arr[i - 1] == 0){
                return false;
            }
            if(status_arr[i - 1] == 0){
                return false;
            }
        }
        return true;
    }


    private void dialog_check_false(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage("表格没有填完,请检查");
        dialog.setPositiveButton("确定", null);
        dialog.show();
    }

    private void dialog_store_success(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setTitle("提示");
        dialog.setMessage("保存成功");
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
                Intent intent = new Intent(activity, Login.class);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    public interface MyListener2{
        abstract void sendMessage2(int code);
    }
}

package com.yzmc.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.yzmc.R;
import com.yzmc.fragment.Fragment_review;
import com.yzmc.util.AllActivity;
import com.yzmc.util.Constant;
import com.yzmc.util.Util_HttpConnect;

import org.json.JSONException;
import org.json.JSONObject;

public class ReviewDetail extends AppCompatActivity {

    private String order_id;
    private String maintain_id;
    private String maintain_type;
    private String maintenceInfo;
    private Fragment_review fragment;
    private ImageView back;
    private Context context = this;
    private int type;
    private boolean isFirst = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_detail);
        AllActivity.addActivity(this);
        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReviewDetail.this, Review.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        showProgressDialog();
        if(isFirst){
            isFirst = false;
            new PageTask().execute();
        }
        super.onResume();
    }

    private class PageTask extends AsyncTask{

        @Override
        protected void onPreExecute() {
            Intent intent = getIntent();
            order_id = intent.getStringExtra("order_id");
            maintain_id = intent.getStringExtra("maintain_id");
            maintain_type = intent.getStringExtra("maintain_type");
            switch (maintain_type){
                case "半月维保":
                    type = 0;
                    break;
                case "季度维保":
                    type = 1;
                    break;
                case "半年维保":
                    type = 2;
                    break;
                case "一年维保":
                    type = 3;
                    break;
            }
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String url3 = Constant.baseUrl + "api/v1/order/maintenance" + type + "?maintain_id=" + maintain_id;
            String result = Util_HttpConnect.getGetResult(url3, context);
            try {
                JSONObject json = new JSONObject(result);
                int code = json.getInt("code");
                if(code != 0){
                    return null;
                }
                String temp = json.getString("obj");
                JSONObject json2 = new JSONObject(temp);
                maintenceInfo = json2.getString("maintainContent");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            //维保单信息已经拿到 就不用再传maintain_id了
            Bundle bundle = new Bundle();
            bundle.putString("order_id", order_id);
            bundle.putString("maintain_type", maintain_type);
            bundle.putString("maintenceInfo", maintenceInfo);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragment = new Fragment_review();
            fragment.setArguments(bundle);
            fragmentTransaction.add(R.id.container, fragment);
            fragmentTransaction.commit();
        }
    }

    public static ProgressDialog progressDialog;

    private void showProgressDialog(){
        progressDialog = new ProgressDialog(ReviewDetail.this);
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

    private void closeProgressDialog(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ReviewDetail.this, Review.class);
        startActivity(intent);
    }
}

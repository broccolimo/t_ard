package com.yzmc.activity;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.yzmc.R;
import com.yzmc.fragment.MainBody1;
import com.yzmc.fragment.MainBody2;
import com.yzmc.fragment.MainTitle1;
import com.yzmc.fragment.MainTitle2;
import com.yzmc.util.AllActivity;

import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity implements View.OnClickListener{

    private long currentTime;
    private LinearLayout tab_1;
    private LinearLayout tab_2;
    private List<View> labs = new ArrayList<>();
    private MainBody1 mainBody1;
    private MainBody2 mainBody2;
    private MainTitle1 mainTitle1;
    private MainTitle2 mainTitle2;
    private ImageView iv_1;
    private ImageView iv_2;
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ActivityCompat.requestPermissions(Main.this,
                new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);


        //注册此Activity
        AllActivity.addActivity(this);
        tab_1 = findViewById(R.id.tab_1);
        tab_2 = findViewById(R.id.tab_2);
        labs.add(tab_1);
        labs.add(tab_2);
        tab_1.setOnClickListener(this);
        tab_2.setOnClickListener(this);
        iv_1 = findViewById(R.id.iv_1);
        iv_2 = findViewById(R.id.iv_2);
    }

    @Override
    protected void onResume() {
        if(isFirst){
            mainBody1 = new MainBody1();
            mainBody2 = new MainBody2();
            mainTitle1 = new MainTitle1();
            mainTitle2 = new MainTitle2();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction
                    .add(R.id.fl_body, mainBody1)
                    .add(R.id.fl_title, mainTitle1)
                    .add(R.id.fl_body, mainBody2).hide(mainBody2)
                    .add(R.id.fl_title, mainTitle2).hide(mainTitle2)
                    .commit();
            tab_1.setClickable(false);
            isFirst = false;
            super.onResume();
            return;
        }

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() - currentTime < 2000){
            for(Activity activity : AllActivity.getAllActivities()){
                activity.finish();
            }
        }
        else{
            Toast.makeText(Main.this, "再按一次退出", Toast.LENGTH_SHORT).show();
            currentTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (v.getId()){
            case R.id.tab_1:
                fragmentTransaction.hide(mainBody2).show(mainBody1).hide(mainTitle2).show(mainTitle1).commit();
                tab_1.setClickable(false);
                tab_2.setClickable(true);
                iv_1.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.mainb1a));
                iv_2.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.mainb2b));
                break;
            case R.id.tab_2:
                fragmentTransaction.hide(mainBody1).show(mainBody2).hide(mainTitle1).show(mainTitle2).commit();
                tab_2.setClickable(false);
                tab_1.setClickable(true);
                iv_1.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.mainb1b));
                iv_2.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.mainb2a));
                break;
            default:
                break;
        }
    }
}

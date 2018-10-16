package com.yzmc.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.yzmc.R;
import com.yzmc.activity.AddElevator;
import com.yzmc.activity.Charge_Back;
import com.yzmc.activity.GIS;
import com.yzmc.activity.Knowledge;
import com.yzmc.activity.Review;
import com.yzmc.activity._Video;
import com.yzmc.activity.b;
import com.yzmc.activity.e;
import com.yzmc.activity.x;
import com.yzmc.util.MyApplication;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//首页
public class MainBody1 extends Fragment implements View.OnClickListener{

    //全局view
    private View view;
    //轮播图的控件
    private ViewPager viewPager;
    //轮播图要用的list
    private List<ImageView> list;
    //所属的Activity
    private Activity activity;
    private Context context;
    private LinearLayout wbgl;
    private LinearLayout wbsh;
    private LinearLayout tdgl;
    private LinearLayout zsk;
    private LinearLayout wxgl;
    private LinearLayout jjzx;
    private LinearLayout zjgl;
    private LinearLayout gis;
    private LinearLayout spjk;

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        int role = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).getInt("role", -1);
        switch (role){
            case 0:
                view = inflater.inflate(R.layout.main_body_1_r0, null);
                give_r0();
                break;
            case 1:
                view = inflater.inflate(R.layout.main_body_1_r1, null);
                give_r1();
                break;
            case 6:
                view = inflater.inflate(R.layout.main_body_1_r6, null);
                give_r6();
                break;
            default:
                break;
        }
        activity = this.getActivity();
        return view;
    }

    private void give_r0(){
        wbgl = view.findViewById(R.id.wbgl);
        wbgl.setOnClickListener(this);
        wbsh = view.findViewById(R.id.wbsh);
        wbsh.setOnClickListener(this);
        zsk = view.findViewById(R.id.zsk);
        zsk.setOnClickListener(this);
        wxgl = view.findViewById(R.id.wxgl);
        wxgl.setOnClickListener(this);
        jjzx = view.findViewById(R.id.jjzx);
        jjzx.setOnClickListener(this);
        gis = view.findViewById(R.id.gis);
        gis.setOnClickListener(this);
        spjk = view.findViewById(R.id.spjk);
        spjk.setOnClickListener(this);
    }

    private void give_r1(){
        wbgl = view.findViewById(R.id.wbgl);
        wbgl.setOnClickListener(this);
        wbsh = view.findViewById(R.id.wbsh);
        wbsh.setOnClickListener(this);
        tdgl = view.findViewById(R.id.tdgl);
        tdgl.setOnClickListener(this);
        zsk = view.findViewById(R.id.zsk);
        zsk.setOnClickListener(this);
        wxgl = view.findViewById(R.id.wxgl);
        wxgl.setOnClickListener(this);
        jjzx = view.findViewById(R.id.jjzx);
        jjzx.setOnClickListener(this);
        spjk = view.findViewById(R.id.spjk);
        spjk.setOnClickListener(this);
        gis = view.findViewById(R.id.gis);
        gis.setOnClickListener(this);
    }

    private void give_r6(){
        zjgl = view.findViewById(R.id.zjgl);
        zjgl.setOnClickListener(this);
        zsk = view.findViewById(R.id.zsk);
        zsk.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Class cls = null;
        switch (v.getId()){
            case R.id.wbgl:
                cls = b.class;
                redirect(cls);
                break;
            case R.id.wbsh:
                cls = Review.class;
                redirect(cls);
                break;
            case R.id.tdgl:
                cls = Charge_Back.class;
                redirect(cls);
                break;
            case R.id.zsk:
                cls = Knowledge.class;
                redirect(cls);
                break;
            case R.id.wxgl:
                cls = x.class;
                redirect(cls);
                break;
            case R.id.jjzx:
                cls = e.class;
                redirect(cls);
                break;
            case R.id.zjgl:
                cls = AddElevator.class;
                redirect(cls);
                break;
            case R.id.spjk:
                cls = _Video.class;
                redirect(cls);
                break;
            case R.id.gis:
                cls = GIS.class;
                redirect(cls);
                break;
            default:
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        viewPager = view.findViewById(R.id.viewPager);
        initViewPager();
    }

    private void initViewPager(){
        ImageView imageView1 = new ImageView(this.getActivity());
        imageView1.setImageResource(R.drawable.p1);
        imageView1.setScaleType(ImageView.ScaleType.FIT_XY);
        ImageView imageView2 = new ImageView(this.getActivity());
        imageView2.setImageResource(R.drawable.p2);
        imageView2.setScaleType(ImageView.ScaleType.FIT_XY);
        ImageView imageView3 = new ImageView(this.getActivity());
        imageView3.setImageResource(R.drawable.p3);
        imageView3.setScaleType(ImageView.ScaleType.FIT_XY);
        list = new ArrayList<>();
        list.add(imageView1);
        list.add(imageView2);
        list.add(imageView3);
        PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView(list.get(position));
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                container.addView(list.get(position));
                return  list.get(position);
            }
        };
        viewPager.setAdapter(pagerAdapter);
    }

    public void redirect(Class cls){
        Intent intent = new Intent(activity, cls);
        startActivity(intent);
    }


    private ProgressDialog progressDialog;

    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        progressDialog.setMessage("正在加载...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}

package com.yzmc.util;

import android.app.Activity;
import java.util.ArrayList;
import java.util.List;

public class AllActivity {
    public static List<Activity> list = new ArrayList<>();
    public static void addActivity(Activity activity){
        list.add(activity);
    }

    public static List<Activity> getAllActivities(){
        return list;
    }
}

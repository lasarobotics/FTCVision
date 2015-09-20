package com.lasarobotics.ftcrobotcontroller;

import android.app.Application;
import android.content.Context;

/**
 * Base application
 */
public class MyApplication extends Application {

    private static Context context;

    public void onCreate(){
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
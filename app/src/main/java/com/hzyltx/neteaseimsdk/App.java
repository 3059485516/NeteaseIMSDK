package com.hzyltx.neteaseimsdk;

import android.support.multidex.MultiDexApplication;

import com.netease.nim.yl.NeteaseApp;

/**
 * @author Yang Shihao
 */
public class App extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        NeteaseApp.neteaseIMInit(this,"",MainActivity.class,MainActivity.class);
    }
}
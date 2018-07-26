package com.android.loadimage;

import android.app.Application;
import android.content.Context;

/**
 * Created by luotao
 * 2018/7/25
 * emil:luotaosc@foxmail.com
 * qq:751423471
 */
public class MyApplication extends Application {
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    /**
     * 获取全局上下文
     * @return
     */
    public static Context getApplication() {
        return mContext;
    }
}

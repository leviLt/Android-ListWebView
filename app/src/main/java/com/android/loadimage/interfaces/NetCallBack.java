package com.android.loadimage.interfaces;

/**
 * Created by luotao
 * 2018/7/25
 * emil:luotaosc@foxmail.com
 * qq:751423471
 */
public interface NetCallBack {
    void success(String html, String url);

    void onFail(Exception e);

    void onStart();
}

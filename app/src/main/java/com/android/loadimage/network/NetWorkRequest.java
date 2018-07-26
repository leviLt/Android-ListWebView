package com.android.loadimage.network;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.android.loadimage.interfaces.NetCallBack;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by luotao
 * 2018/7/25
 * emil:luotaosc@foxmail.com
 * qq:751423471
 *
 * @author 罗涛
 */
public class NetWorkRequest implements Runnable {
    private NetCallBack mCallback;
    private String url;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case 0:
                    mCallback.onFail((Exception) msg.obj);
                    break;
                case 1:
                    mCallback.success((String) msg.obj, url);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * @param url      网页链接
     * @param callback 回调
     */
    public NetWorkRequest(String url, NetCallBack callback) {
        this.mCallback = callback;
        this.url = url;
    }

    @Override
    public void run() {
        try {
            Message message1 = mHandler.obtainMessage();
            message1.arg1 = 2;
            mHandler.sendMessage(message1);
            URL url = new URL(this.url);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == 200) {
                InputStream inputStream = connection.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                String html = new String(outputStream.toByteArray(), "utf-8");
                Message message = mHandler.obtainMessage();
                message.obj = html;
                //1 请求成功  0请求失败
                message.arg1 = 1;
                mHandler.sendMessage(message);
                //关闭流
                inputStream.close();
                outputStream.close();
            }
        } catch (Exception e) {
            Message message = mHandler.obtainMessage();
            message.arg1 = 0;
            message.obj = e;
            mHandler.sendMessage(message);
        }
    }
}

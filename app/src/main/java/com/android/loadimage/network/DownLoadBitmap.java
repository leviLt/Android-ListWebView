package com.android.loadimage.network;

import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.loadimage.utils.LruCacheUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Scorpio on 2018/7/25.
 * QQ:751423471
 * phone:13982250340
 */

public class DownLoadBitmap implements Runnable {
    private static final String TAG = "DownLoadBitmap";
    private String path;

    public DownLoadBitmap(String path) {
        this.path = path;

    }

    @Override
    public void run() {
        try {
            URL url = new URL(path);
            //这里添加的网页都是https的，暂未写http的
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == 200) {
                InputStream inputStream = connection.getInputStream();
                Log.e(TAG, "下载Bitmap成功  url==" + path);
                LruCacheUtil.getInstance().putBitmapCache(path, BitmapFactory.decodeStream(inputStream));
                inputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "下载Bitmap失败  url==" + path);
        }
    }
}

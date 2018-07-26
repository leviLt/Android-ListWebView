package com.android.loadimage.utils;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

/**
 * Created by luotao
 * 2018/7/18
 * emil:luotaosc@foxmail.com
 * qq:751423471
 */
public class LruCacheUtil {
    public static LruCacheUtil instance;
    /**
     * html缓存
     */
    private LruCache<String, String> mLruCache;
    /**
     * 图片的缓存
     */
    private LruCache<String, Bitmap> mBitmapLruCache;

    //图片缓存
    private LruCacheUtil() {
    }

    /**
     * 获取缓存Html的LruCache
     */
    private synchronized void getLruCacheInstance() {
        if (mLruCache != null) {
            return;
        }
        int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 16);
        mLruCache = new LruCache<String, String>(cacheSize) {
            @Override
            protected int sizeOf(String key, String value) {
                return value.getBytes().length;
            }
        };
    }

    /**
     * 获取缓存Html的LruCache
     */
    private synchronized void getBitmapCacheInstance() {
        if (mBitmapLruCache != null) {
            return;
        }
        int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 16);
        mBitmapLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    /**
     * 单例模式
     *
     * @return
     */
    public static LruCacheUtil getInstance() {
        if (instance == null) {
            synchronized (LruCacheUtil.class) {
                if (instance == null) {
                    instance = new LruCacheUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 存入Html
     *
     * @param url
     * @param html
     */
    public void putHtmlCache(String url, String html) {
        if (mLruCache == null) {
            getLruCacheInstance();
        }
        if (getHtmlFromCache(url) == null) {
            mLruCache.put(url, html);
            Log.e("putHtmlCache", "putHtmlCache================= " + "success");
        }
    }

    /**
     * 获取缓存Html
     *
     * @param url
     * @return
     */
    public String getHtmlFromCache(String url) {
        if (mLruCache == null) {
            getLruCacheInstance();
        }
        return mLruCache.get(url);
    }


    /**
     * 存入bitmap
     *
     * @param url
     * @param bitmap
     */
    public void putBitmapCache(String url, Bitmap bitmap) {
        if (mBitmapLruCache == null) {
            getBitmapCacheInstance();
        }
        if (getHtmlFromCache(url) == null) {
            mBitmapLruCache.put(url, bitmap);
            Log.e("putBitmapCache", "putBitmapCache================= " + "success");
        }
    }

    /**
     * 获取缓存Html
     *
     * @param url
     * @return
     */
    public Bitmap getBitmapFromCache(String url) {
        if (mBitmapLruCache == null) {
            getBitmapCacheInstance();
        }
        return mBitmapLruCache.get(url);
    }
}

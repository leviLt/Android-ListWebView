package com.android.loadimage;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.android.loadimage.network.DownLoadBitmap;
import com.android.loadimage.network.ThreadPoolManger;
import com.android.loadimage.utils.LruCacheUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class WebViewActivity extends AppCompatActivity {
    public static final String GET = "GET";
    private static final String TAG = "WebViewActivity";
    public static final String PNG = ".png";
    public static final String JPG = ".jpg";
    WebView mWebView;
    FrameLayout container;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        url = getIntent().getStringExtra(ListActivity.URL_);
        initView();
    }

    private void initView() {
        container = findViewById(R.id.container);
        mWebView = new WebView(this);
        //配置WebView
        webViewConfig();

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
        );
        container.addView(mWebView, layoutParams);
        if (url != null) {
            String htmlFromCache = LruCacheUtil.getInstance().getHtmlFromCache(url);
            if (htmlFromCache != null) {
                Log.e(TAG, "加载Html从缓存中");
                mWebView.loadDataWithBaseURL(url, htmlFromCache, "text/html", "utf-8", null);
                //                mWebView.loadData(htmlFromCache, "text/html", "utf-8");
            } else {
                mWebView.loadUrl(url);
            }
        } else {
            mWebView.loadUrl("about:blank");
        }
    }

    /**
     * webView 配置
     */
    private void webViewConfig() {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new MyWebViewClient());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        container.removeView(mWebView);
        if (mWebView != null) {
            mWebView.stopLoading();
            mWebView.loadUrl("about:blank");
            mWebView = null;
        }
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (!GET.equals(request.getMethod())) {
                return super.shouldInterceptRequest(view, request);
            }
            /*
             * js: mimeType = "application/x-javascript";
             css: mimeType = "text/css";
             html: mimeType = "text/html";
             jpg/png:  mimeType = "image/png";
             */
            String url = request.getUrl().toString();
            if (url.endsWith(PNG) || url.endsWith(JPG)) {
                Log.e(TAG, "url====" + url);
                Bitmap bitmap = LruCacheUtil.getInstance().getBitmapFromCache(url);
                if (bitmap != null) {
                    String mimeType = "image/png";
                    String encoding = "utf-8";
                    //bitmap 转换成 inputStream
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
                    Log.e(TAG, "加载缓存中的图片");
                    WebResourceResponse response = new WebResourceResponse(mimeType, encoding, inputStream);
                    try {
                        //关闭流
                        baos.close();
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return response;
                } else {
                    //缓存中不存在就需要下载
                    ThreadPoolManger.getInstance().execute(new DownLoadBitmap(url));
                }
                return super.shouldInterceptRequest(view, request);
            } else {
                return super.shouldInterceptRequest(view, request);
            }
        }
    }
}

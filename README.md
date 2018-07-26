##实现效果
![示例](https://github.com/scorpioLt/Android-ListWebView/blob/master/gif/listWebView.gif) 
这是运行的效果

##简单解释
1.列表使用的是RecyclerView，实现滑动3s之后再缓存网页HTML，每个Item对应于一个HTML。
每个HTML获得源码、缓存之后就给Item一个标记
###代码：
####（1）判断滑动3s之后开始预加载：
```
 mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            CountDownTimer countDownTimer;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        Log.e(TAG, "onScrollStateChanged: " + "SCROLL_STATE_IDLE");
                        //3s 之后就开始加载数据
                        countDownTimer = new CountDownTimer(3000, 3000) {

                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {
                                mAdapter.loadData();
                            }
                        };
                        countDownTimer.start();
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        Log.e(TAG, "onScrollStateChanged: " + "SCROLL_STATE_DRAGGING");
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        Log.e(TAG, "onScrollStateChanged: " + "SCROLL_STATE_SETTLING");
                        break;
                    default:
                        break;
                }
            }
```
####（2）线程池去获取Html源码：
adapter中的调用方法
```
   public void loadData() {
        Log.e(TAG, "3s 后加载数据 ");
        if (!isLoad) {
            ThreadPoolManger.getInstance().run();
            isLoad = true;
        }
    }
```
线程池中的添加任务和运行任务的方法：
```
   /**
     * 添加任务
     */
    public void addRunnable(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        activeThread.add(runnable);
    }

    /**
     * 真正开始执行
     */
    public void run() {
        if (activeThread.size() <= 0) {
            return;
        }
        for (int i = 0; i < activeThread.size(); i++) {
            execute(activeThread.get(i));
        }
    }
```
下载HTML方法以及回调，切换主线程：
```
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
```
2.点击列表加载缓存HTML和缓存图片
核心代码
```
//1.加载缓存中的HTML
mWebView.loadDataWithBaseURL(url, htmlFromCache, "text/html", "utf-8", null);
//2.缓存没有图片就下载，有就直接返回

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
```
 

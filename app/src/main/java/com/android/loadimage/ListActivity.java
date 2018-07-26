package com.android.loadimage;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.loadimage.adapter.ListAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private static final String TAG = "ListActivity";
    public static final String URL_ = "url";
    private RecyclerView mRecyclerView;
    private ListAdapter mAdapter;
    private List<String> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mRecyclerView = findViewById(R.id.recycler_view);
        initView();
    }

    private void initView() {
        String[] urls = getResources().getStringArray(R.array.url);
        if (urls.length > 0) {
            mList = Arrays.asList(urls);
        } else {
            mList = new ArrayList<>();
        }
        mAdapter = new ListAdapter(mList, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new ListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(ListActivity.this, WebViewActivity.class);
                intent.putExtra(URL_, mList.get(position));
                startActivity(intent);
            }
        });
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

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

package com.android.loadimage.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.loadimage.R;
import com.android.loadimage.interfaces.NetCallBack;
import com.android.loadimage.network.NetWorkRequest;
import com.android.loadimage.network.ThreadPoolManger;
import com.android.loadimage.utils.LruCacheUtil;

import java.util.List;

/**
 * Created by luotao
 * 2018/7/18
 * emil:luotaosc@foxmail.com
 * qq:751423471
 *
 * @author 罗涛
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.MyViewHolder> {
    private static final String TAG = "ListAdapter";
    private List<String> mArrayList;
    private Context mContext;
    private boolean isLoad = false;

    public ListAdapter(List<String> arrayList, Context context) {
        mArrayList = arrayList;
        mContext = context;
    }

    @NonNull
    @Override
    public ListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ListAdapter.MyViewHolder holder, int position) {
        holder.tvName.setText(mContext.getString(R.string.natural) + position);
        holder.mProgressBar.setVisibility(View.GONE);
        holder.flag.setVisibility(View.GONE);
        if (LruCacheUtil.getInstance().getHtmlFromCache(mArrayList.get(position)) != null) {
            holder.mProgressBar.setVisibility(View.GONE);
            holder.flag.setImageResource(R.drawable.ic_done_black_24dp);
            holder.flag.setVisibility(View.VISIBLE);
            return;
        }
        ThreadPoolManger.getInstance().addRunnable(new NetWorkRequest(mArrayList.get(position), new NetCallBack() {
            @Override
            public void onStart() {
                holder.mProgressBar.setVisibility(View.VISIBLE);
                holder.flag.setVisibility(View.GONE);
            }

            @Override
            public void success(String html, String url) {
                Log.e(TAG, html);
                holder.mProgressBar.setVisibility(View.GONE);
                holder.flag.setImageResource(R.drawable.ic_done_black_24dp);
                holder.flag.setVisibility(View.VISIBLE);
                //缓存Html到内存中
                LruCacheUtil.getInstance().putHtmlCache(url, html);
            }

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail: " + e.toString());
                holder.mProgressBar.setVisibility(View.GONE);
                holder.flag.setImageResource(R.drawable.ic_close_black_24dp);
                holder.flag.setVisibility(View.VISIBLE);
            }
        }));
        //点击事件
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(holder.getLayoutPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void loadData() {
        Log.e(TAG, "3s 后加载数据 ");
        if (!isLoad) {
            ThreadPoolManger.getInstance().run();
            isLoad = true;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView img;
        private AppCompatTextView tvName;
        private ProgressBar mProgressBar;
        private ImageView flag;

        public MyViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
            tvName = itemView.findViewById(R.id.name);
            mProgressBar = itemView.findViewById(R.id.progress);
            flag = itemView.findViewById(R.id.flag);
        }
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}

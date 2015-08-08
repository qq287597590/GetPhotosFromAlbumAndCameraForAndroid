package com.example.wangxiaolong.getphotosforandroid.adapter;/**
 * Created by dengxin on 6/2/15.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * User: Deng xin
 * Emial:dengxin@meizu.com
 * Date: 2015-06-02
 * Time: 20:09
 * Description: Listview的BaseAdapter
 */

public abstract class ListViewBaseAdapter<T> extends BaseAdapter {

    private LayoutInflater mInflater;

    public ListViewBaseAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    protected LayoutInflater getInflater() {
        return mInflater;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public void clear() {
    }


}

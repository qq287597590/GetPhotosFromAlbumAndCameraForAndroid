package com.example.wangxiaolong.getphotosforandroid.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.example.wangxiaolong.getphotosforandroid.R;
import com.example.wangxiaolong.getphotosforandroid.activity.GalleryActivity;
import com.example.wangxiaolong.getphotosforandroid.activity.ImageScanActivity;
import com.example.wangxiaolong.getphotosforandroid.utils.LocalBitmapLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: Wang Xiao Long.
 * Email: wangxiaolong@meizu.com
 * Date: 2015-08-07
 * Time: 10:46
 * Description: 图库内容适配器
 */
public class GalleryAdapter extends ListViewBaseAdapter {
    private List<String> mImageList = null;
    Map<Integer, Boolean> checkedMap = new HashMap<Integer, Boolean>();
    private Map<Integer, View> childViewMap = new HashMap<Integer, View>();
    private int choseCount = 0;
    private Context mContext;
    private Point mPoint;
    private Handler mHandler;

    public GalleryAdapter(List<String> imageList, Context context, Handler handler) {
        super(context);
        this.mImageList = imageList;
        this.mContext = context;
        this.mHandler = handler;
        mPoint = new Point(250, 250);         //缩略图高宽
    }

    public void changeImageList(List<String> imageList) {
        mImageList = imageList;
        choseCount = 0;
        checkedMap.clear();
        mHandler.sendEmptyMessage(GalleryActivity.EVENT_CHOSE);
        notifyDataSetChanged();
    }

    public int getChoseCount() {
        return choseCount;
    }

    @Override
    public int getCount() {
        return mImageList.size();
    }

    @Override
    public Object getItem(int position) {
        return mImageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = getInflater().inflate(R.layout.gallery_picture_item, null);
            viewHolder.ImageView = (ImageView) convertView.findViewById(R.id.item_image);
            viewHolder.CheckView = (CheckBox) convertView.findViewById(R.id.item_check);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (checkedMap.get(position) == null) {
            checkedMap.put(position, false);
        }
        viewHolder.ImageView.setImageResource(R.drawable.default_avatar);
        viewHolder.CheckView.setChecked(checkedMap.get(position));
        viewHolder.ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("path", mImageList.get(position));
                intent.setClass(mContext, ImageScanActivity.class);
                mContext.startActivity(intent);
            }
        });
        viewHolder.CheckView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                checkedMap.put(position, checked);
                if (checked) {                        //checked animation
                    choseCount++;
                    ViewHolder viewHolder = (ViewHolder) childViewMap.get(position).getTag();

                } else {
                    choseCount--;
                }
                mHandler.sendEmptyMessage(GalleryActivity.EVENT_CHOSE);
            }
        });

        childViewMap.put(position, convertView);
        return convertView;
    }

    /**
     * 返回选中照片路径的列表
     *
     * @return 选中照片的路径列表
     */
    public ArrayList<CharSequence> getSelectItems() {
        ArrayList<CharSequence> list = new ArrayList<CharSequence>();
        for (Iterator<Map.Entry<Integer, Boolean>> it = checkedMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Boolean> entry = it.next();
            if (entry.getValue()) {
                list.add(mImageList.get(entry.getKey()));
            }
        }
        return list;
    }

    public void LoadLocalImage(final int position) {
        LocalBitmapLoader.instance().loadLocalImage(mImageList.get(position), mPoint, new LocalBitmapLoader.LocalImageLoaderCallback() {
            @Override
            public void onImageLoaded(Bitmap bitmap, String path) {
                ViewHolder viewHolder = (ViewHolder) childViewMap.get(position).getTag();
                viewHolder.ImageView.setImageBitmap(bitmap);
                viewHolder.CheckView.setChecked(checkedMap.get(position));
            }
        });
    }


    public class ViewHolder {
        public ImageView ImageView;
        public CheckBox CheckView;
    }
}

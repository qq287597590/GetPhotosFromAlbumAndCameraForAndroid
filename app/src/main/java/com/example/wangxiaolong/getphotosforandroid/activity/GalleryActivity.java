package com.example.wangxiaolong.getphotosforandroid.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.example.wangxiaolong.getphotosforandroid.R;
import com.example.wangxiaolong.getphotosforandroid.adapter.GalleryAdapter;
import com.example.wangxiaolong.getphotosforandroid.adapter.ListViewBaseAdapter;
import com.example.wangxiaolong.getphotosforandroid.bean.ImageBean;
import com.example.wangxiaolong.getphotosforandroid.utils.LocalBitmapLoader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * User: Wang Xiao Long.
 * Email: wangxiaolong@meizu.com
 * Date: 2015-08-04
 * Time: 17:29
 * Description: 相册照片选择
 */
public class GalleryActivity extends Activity implements AbsListView.OnScrollListener, View.OnClickListener {
    private static final String TAG = "GalleryActivity";
    private HashMap<String, List<String>> mGruopMap = new HashMap<String, List<String>>();
    private List<ImageBean> mImageAlbumList = new ArrayList<ImageBean>();
    private List<String> mAllImageList = new ArrayList<String>();
    public static final int EVENT_CHOSE = 0X123;
    private Spinner mAlbumSpinner;
    private GridView mGalleryGridview;
    private Button mConfirmButton;
    private AlbumAdapter mAlbumAdapter;
    private GalleryAdapter mGalleryAdapter;
    private final String ALL_IMAGE = "All images";
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == EVENT_CHOSE) {
                int count = mGalleryAdapter.getChoseCount();
                mConfirmButton.setText(String.format("%s(%d)", getResources().getString(R.string.chose_image_confirm), count));
            }
        }
    };
    AsyncTask loadImageTask = new AsyncTask() {
        @Override
        protected Object doInBackground(Object[] params) {
            Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver mContentResolver = getContentResolver();
            //只查询jpeg和png的图片
            Cursor mCursor = mContentResolver.query(mImageUri, null,
                    MediaStore.Images.Media.MIME_TYPE + "=? or "
                            + MediaStore.Images.Media.MIME_TYPE + "=?",
                    new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);
            if (mCursor == null) {
                return null;
            }
            while (mCursor.moveToNext()) {
                String path = mCursor.getString(mCursor   //获取图片的路径
                        .getColumnIndex(MediaStore.Images.Media.DATA));
                String parentName = new File(path).getParentFile().getName();  //获取该图片的父路径名
                // 根据父路径名将图片放入到mGruopMap中
                mAllImageList.add(path);
                if (!mGruopMap.containsKey(parentName)) {
                    List<String> childList = new ArrayList<String>();
                    childList.add(path);
                    mGruopMap.put(parentName, childList);
                } else {
                    mGruopMap.get(parentName).add(path);
                }
            }
            mCursor.close();
            return null;
        }

        //扫描图片完成
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            mImageAlbumList = subGroupOfImage(mGruopMap);
            mAlbumAdapter = new AlbumAdapter(mImageAlbumList, GalleryActivity.this);
            mGalleryAdapter = new GalleryAdapter(mAllImageList, GalleryActivity.this, mHandler);
            mAlbumSpinner.setAdapter(mAlbumAdapter);
            mGalleryGridview.setAdapter(mGalleryAdapter);
            mAlbumSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mGalleryAdapter.changeImageList(mGruopMap.get(mImageAlbumList.get(position).getFolderName()));
                    loadVisibleImages();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            mAlbumSpinner.setSelection(0);
            mHandler.sendEmptyMessage(GalleryActivity.EVENT_CHOSE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        LocalBitmapLoader.instance();
        initViews();
    }

    private void loadVisibleImages() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int last = mGalleryGridview.getLastVisiblePosition();
                int first = mGalleryGridview.getFirstVisiblePosition();
                for (int i = first; i <= last; i++) {
                    mGalleryAdapter.LoadLocalImage(i);
                }
            }
        }, 100);
    }

    private void initViews() {
        mAlbumSpinner = (Spinner) findViewById(R.id.album_category);
        mGalleryGridview = (GridView) findViewById(R.id.album_photos);
        mConfirmButton = (Button) findViewById(R.id.image_chose_confirm);
        mGalleryGridview.setOnScrollListener(this);
        mConfirmButton.setOnClickListener(this);
        ((ImageView) findViewById(R.id.activity_top_back_iv)).setOnClickListener(this);
        getImages();
    }

    private void getImages() {

        loadImageTask.execute();
    }

    /**
     * 组装分组界面GridView的数据源，因为我们扫描手机的时候将图片信息放在HashMap中
     * 所以需要遍历HashMap将数据组装成List
     *
     * @param mGruopMap
     * @return
     */
    private List<ImageBean> subGroupOfImage(HashMap<String, List<String>> mGruopMap) {
        if (mGruopMap.size() == 0) {
            return null;
        }
        List<ImageBean> list = new ArrayList<ImageBean>();
        ImageBean allImageBean = new ImageBean();
        allImageBean.setFolderName(ALL_IMAGE);                     //增加一个包含所有照片的相册描述
        allImageBean.setImageCounts(mAllImageList.size());
        allImageBean.setTopImagePath(mAllImageList.get(0));
        list.add(allImageBean);
        Iterator<Map.Entry<String, List<String>>> it = mGruopMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            ImageBean mImageBean = new ImageBean();
            String key = entry.getKey();
            List<String> value = entry.getValue();
            mImageBean.setFolderName(key);
            mImageBean.setImageCounts(value.size());
            mImageBean.setTopImagePath(value.get(0));//获取该组的第一张图片
            list.add(mImageBean);
        }
        mGruopMap.put(ALL_IMAGE, mAllImageList);   //包含所有相片的相册列表
        return list;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            loadVisibleImages();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.image_chose_confirm) {
            Intent intent = new Intent();
            intent.putCharSequenceArrayListExtra("images", mGalleryAdapter.getSelectItems());
            setResult(RESULT_OK, intent);
            finish();
        }
        if (v.getId() == R.id.activity_top_back_iv) {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}

/**
 * 相册适配器类
 */
class AlbumAdapter extends ListViewBaseAdapter {
    private List<ImageBean> mImageAlbumList = null;
    private Context mContext;
    private Point mPoint;

    AlbumAdapter(List<ImageBean> imageAlbumList, Context context) {
        super(context);
        this.mImageAlbumList = imageAlbumList;
        this.mContext = context;
        mPoint = new Point(250, 250);
    }

    @Override
    public int getCount() {
        return mImageAlbumList == null? 0 : mImageAlbumList.size();
    }

    @Override
    public Object getItem(int position) {
        return mImageAlbumList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = getInflater().inflate(R.layout.gallery_album_item, null);
            viewHolder.albumName = (TextView) convertView.findViewById(R.id.album_name);
            viewHolder.albumChecked = (ImageView) convertView.findViewById(R.id.album_check);
            viewHolder.albumImage = (ImageView) convertView.findViewById(R.id.album_first);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final ImageView firstImage = viewHolder.albumImage;
        ImageBean imageBean = ((ImageBean) mImageAlbumList.get(position));
        LocalBitmapLoader.instance().loadLocalImage(imageBean.getTopImagePath(), mPoint, new LocalBitmapLoader.LocalImageLoaderCallback() {
            @Override
            public void onImageLoaded(Bitmap bitmap, String path) {
                firstImage.setImageBitmap(bitmap);
            }
        });
        viewHolder.albumName.setText(String.format("%s (%d)", imageBean.getFolderName(), imageBean.getImageCounts()));
        return convertView;
    }

    class ViewHolder {
        ImageView albumImage;
        TextView albumName;
        ImageView albumChecked;
    }
}

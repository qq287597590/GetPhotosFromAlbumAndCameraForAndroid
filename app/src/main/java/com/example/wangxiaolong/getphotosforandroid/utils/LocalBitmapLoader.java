package com.example.wangxiaolong.getphotosforandroid.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: Wang Xiao Long.
 * Email: wangxiaolong@meizu.com
 * Date: 2015-08-06
 * Time: 17:15
 * Description: 本地图片加载类
 */
public class LocalBitmapLoader {
    public static LocalBitmapLoader mInstance = null;
    private ExecutorService mImageThreadPool = Executors.newFixedThreadPool(3);
    private LruImageCache mMemoryCache;

    private LocalBitmapLoader() {
        mMemoryCache = LruImageCache.instance();
    }

    public static LocalBitmapLoader instance() {
        if (mInstance == null)
            mInstance = new LocalBitmapLoader();
        return mInstance;
    }

    /**
     * 此方法来加载本地图片，这里的mPoint是用来封装ImageView的宽和高，我们会根据ImageView控件的大小来裁剪Bitmap
     * 如果你不想裁剪图片，调用loadNativeImage(final String path, final NativeImageCallBack mCallBack)来加载
     *
     * @param path
     * @param mPoint
     * @param mCallBack UI thread to do with this interface
     * @return
     */

    public Bitmap reloadLocalImage(final String path, final Point mPoint, final LocalImageLoaderCallback mCallBack){
        removeBitmapFromMemCache(path);
        return loadLocalImage(path,mPoint,mCallBack);
    }

    public Bitmap loadLocalImage(final String path, final Point mPoint, final LocalImageLoaderCallback mCallBack) {
        //先获取内存中的Bitmap
        Bitmap bitmap = getBitmapFromMemCache(path);

        final Handler mHander = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mCallBack.onImageLoaded((Bitmap) msg.obj, path);
            }

        };

        //若该Bitmap不在内存缓存中，则启用线程去加载本地的图片，并将Bitmap加入到mMemoryCache中
        if (bitmap == null) {
            mImageThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    //先获取图片的缩略图
                    Bitmap mBitmap = decodeThumbBitmapForFile(path, mPoint == null ? 0 : mPoint.x, mPoint == null ? 0 : mPoint.y);
                    Message msg = mHander.obtainMessage();
                    msg.obj = mBitmap;
                    mHander.sendMessage(msg);
                    //将图片加入到内存缓存
                    addBitmapToMemoryCache(path, mBitmap);
                }
            });
        } else {
            mCallBack.onImageLoaded(bitmap, path);
        }
        return bitmap;

    }

    /**
     * 往内存缓存中添加Bitmap
     *
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.putBitmap(key, bitmap);
        }
    }

    /**
     * * 将缓存中的文件删除，如果存在
     * @param key 路径
     */
    private void removeBitmapFromMemCache(String key){
        if(key !=null && getBitmapFromMemCache(key) != null){
            mMemoryCache.removeBitmap(key);
        }
    }

    /**
     * 根据key来获取内存中的图片
     *
     * @param key
     * @return
     */
    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.getBitmap(key);
    }

    private Bitmap decodeThumbBitmapForFile(String path, int viewWidth, int viewHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //设置为true,表示解析Bitmap对象，该对象不占内存
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        //设置缩放比例
        options.inSampleSize = computeScale(options, viewWidth, viewHeight);
        //设置为false,表示解析Bitmap对象，并为bitmap分配内存
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 根据View(主要是ImageView)的宽和高来计算Bitmap缩放比例。默认不缩放
     *
     * @param options
     * @param viewWidth
     * @param viewHeight
     */
    private int computeScale(BitmapFactory.Options options, int viewWidth, int viewHeight) {
        int inSampleSize = 1;
        if (viewWidth == 0 || viewWidth == 0) {
            return inSampleSize;
        }
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        //假如Bitmap的宽度或高度大于我们设定图片的View的宽高，则计算缩放比例
        if (bitmapWidth > viewWidth || bitmapHeight > viewWidth) {
            int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
            int heightScale = Math.round((float) bitmapHeight / (float) viewWidth);

            //为了保证图片不缩放变形，我们取宽高比例最小的那个
            inSampleSize = widthScale < heightScale ? widthScale : heightScale;
        }
        return inSampleSize;
    }


    /**
     * 加载本地图片的回调接口
     *
     * @author xiaanming
     */
    public interface LocalImageLoaderCallback {
        /**
         * 当子线程加载完了本地的图片，将Bitmap和图片路径回调在此方法中
         *
         * @param bitmap
         * @param path
         */
        public void onImageLoaded(Bitmap bitmap, String path);
    }
}

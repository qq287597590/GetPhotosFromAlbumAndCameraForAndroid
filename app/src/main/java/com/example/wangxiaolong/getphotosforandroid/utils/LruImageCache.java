package com.example.wangxiaolong.getphotosforandroid.utils;/**
 * Created by dengxin on 6/13/15.
 */

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
/**
 * User: dengxin
 * Email:dengxin@meizu.com
 * Date: 2015-07-10
 * Time: 16:33
 * Description: 主要用于配合VolleyNetWorkImageView中的图片缓存模块
 */

public class LruImageCache {

    private static LruCache<String, Bitmap> mMemoryCache;

    private static LruImageCache lruImageCache;

    private LruImageCache(){
        // Get the Max available memory
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap){
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    public static LruImageCache instance(){
        if(lruImageCache == null){
            lruImageCache = new LruImageCache();
        }
        return lruImageCache;
    }

    public Bitmap getBitmap(String arg0) {
        return mMemoryCache.get(arg0);
    }

    /**
     * 删除指定地址的图片缓存
     * @param arg0 图片路径
     */
    public void removeBitmap(String arg0){
        mMemoryCache.remove(arg0);
    }
    public void putBitmap(String arg0, Bitmap arg1) {
        if(getBitmap(arg0) == null){
            mMemoryCache.put(arg0, arg1);
        }
    }


}

package com.example.wangxiaolong.getphotosforandroid.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.example.wangxiaolong.getphotosforandroid.R;
import com.example.wangxiaolong.getphotosforandroid.utils.LocalBitmapLoader;

/**
 * User: Wang Xiao Long.
 * Email: wangxiaolong@meizu.com
 * Date: 2015-08-07
 * Time: 21:07
 * Description:
 */
public class ImageScanActivity extends Activity implements View.OnTouchListener {
    ImageView mImage = null;
    ImageView mBack = null;
    //放大缩小
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist;
    //模式
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;
    Point mPoint = new Point(800, 600);
    Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String path = getIntent().getStringExtra("path");
        setContentView(R.layout.activity_view_image);
        mImage = (ImageView) findViewById(R.id.image_scan);


        mBack = (ImageView) findViewById(R.id.activity_top_back_iv);

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        LocalBitmapLoader.instance().reloadLocalImage(path, mPoint, new LocalBitmapLoader.LocalImageLoaderCallback() {
            @Override
            public void onImageLoaded(Bitmap bitmap, String path) {
                mImage.setImageBitmap(bitmap);
                mImage.setScaleType(ImageView.ScaleType.MATRIX);


            }
        });
        mImage.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView myImageView = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            //设置拖拉模式
            case MotionEvent.ACTION_DOWN:
                matrix.set(myImageView.getImageMatrix());
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;

            //设置多点触摸模式
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            //若为DRAG模式，则点击移动图片
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                }
                //若为ZOOM模式，则点击触摸缩放
                else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        //设置硕放比例和图片的中点位置
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }
        myImageView.setImageMatrix(matrix);
        return true;
    }

    //计算移动距离
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    //计算中点位置
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    //自动居中  左右及上下都居中
   /* protected void center()
    {
        center(true,true);
    }

    private void center(boolean horizontal, boolean vertical)
    {
        Matrix m = new Matrix();
        m.set(matrix);
        RectF rect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        m.mapRect(rect);
        float height = rect.height();
        float width = rect.width();
        float deltaX = 0, deltaY = 0;
        if (vertical)
        {
            //int screenHeight = dm.heightPixels;  //手机屏幕分辨率的高度
            int screenHeight = 400;
            if (height < screenHeight)
            {
                deltaY = (screenHeight - height)/2 - rect.top;
            }else if (rect.top > 0)
            {
                deltaY = -rect.top;
            }else if (rect.bottom < screenHeight)
            {
                deltaY = view.getHeight() - rect.bottom;
            }
        }

        if (horizontal)
        {
            //int screenWidth = dm.widthPixels;  //手机屏幕分辨率的宽度
            int screenWidth = 400;
            if (width < screenWidth)
            {
                deltaX = (screenWidth - width)/2 - rect.left;
            }else if (rect.left > 0)
            {
                deltaX = -rect.left;
            }else if (rect.right < screenWidth)
            {
                deltaX = screenWidth - rect.right;
            }
        }
        matrix.postTranslate(deltaX, deltaY);
    }*/
}


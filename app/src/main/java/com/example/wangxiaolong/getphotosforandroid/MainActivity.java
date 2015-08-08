package com.example.wangxiaolong.getphotosforandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.wangxiaolong.getphotosforandroid.activity.GalleryActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener{

    Context mContext;
    private final int WITH_COMMIT_CAPTURE = 1;
    private final int WITH_COMMIT_ALBUM = 0;
    String CAMERA_PATH = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        findViewById(R.id.capture).setOnClickListener(this);
        findViewById(R.id.album).setOnClickListener(this);
    }

    private void OpenAlbum(){
        Intent intent = new Intent();
        intent.setClass(mContext, GalleryActivity.class);
        startActivityForResult(intent, WITH_COMMIT_ALBUM);
    }
    /**
     * 打开照相机
     */
    private void OpenCamera() {
        CAMERA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mContext.getPackageName();
        File file = new File(CAMERA_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        CAMERA_PATH += "/cameraImg" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        Uri mUri = Uri.fromFile(new File(CAMERA_PATH));
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.addCategory(Intent.CATEGORY_DEFAULT);
        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mUri);
        cameraIntent.putExtra("return-data", true);
        startActivityForResult(cameraIntent, WITH_COMMIT_CAPTURE);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WITH_COMMIT_CAPTURE) {
            File file = new File(CAMERA_PATH);
            if (file.exists()) {
                List<String> images = new ArrayList<String>();
                images.add(CAMERA_PATH);    //CAMERA_PATH is the result that you want

            }
        }
        if (requestCode == WITH_COMMIT_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {   //成功选择照片
                ArrayList<CharSequence> imagesGot = data.getCharSequenceArrayListExtra("images");
                List<String> images = new ArrayList<String>();
                for (int i = 0; i < imagesGot.size(); i++) {
                    String path = imagesGot.get(i).toString();
                    File file = new File(path);
                    if (file.exists()) {
                        images.add(path);
                    }
                }
                // Object images is the result of the path for image files
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.capture){
            OpenCamera();
        }
        if(v.getId() == R.id.album){
            OpenAlbum();
        }
    }
}

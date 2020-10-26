package org.greenleaf.mav;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MediaRecordActivity extends AppCompatActivity {

    private static final int LOCAL_REQUEST_CODE = 11;
    private static final String TAG = "MediaRecordActivity";
    private MediaProjectionManager mProjectionManager;
    private MediaRecordService mMediaRecordService;
    private ScreenRecordService mScreenRecordService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_record);

        Button button = findViewById(R.id.start);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionsUtil.requestPermission(MediaRecordActivity.this, new PermissionListener() {
                    @Override
                    public void permissionGranted(@NonNull String[] permission) {
                        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                        Intent captureIntent = mProjectionManager.createScreenCaptureIntent();
                        startActivityForResult(captureIntent, LOCAL_REQUEST_CODE);
                    }

                    @Override
                    public void permissionDenied(@NonNull String[] permission) {
                    }
                }, Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });

        Button button2 = findViewById(R.id.stop);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaRecordService != null) mMediaRecordService.release();
                if (mScreenRecordService != null) mScreenRecordService.quit();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        doStartRecorder(resultCode, data);
        doStartScreenRecorder(resultCode, data);
    }

    private void doStartRecorder(int resultCode, @Nullable Intent data) {
        MediaProjection mediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection == null) {
            Log.e(TAG, "media projection is null");
            return;
        }

        Point outSize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(outSize);
        int displayWidth = outSize.x;
        int displayHeight = outSize.y;

        String strDateFormat = "HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strDateFormat);
        Log.d(TAG, String.format("recorder-%1$s.mp4", simpleDateFormat.format(new Date())));
        File file = new File(getExternalFilesDir(""), String.format("recorder-%1$s.mp4", simpleDateFormat.format(new Date()))); //录屏生成文件
        if (mMediaRecordService != null) {
            mMediaRecordService.release();
            mMediaRecordService = null;
        }
        mMediaRecordService = new MediaRecordService(displayWidth, displayHeight, 6000000, 1,
                mediaProjection, file.getAbsolutePath());
        mMediaRecordService.start();
    }

    private void doStartScreenRecorder(int resultCode, @Nullable Intent data) {
        MediaProjection mediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        if (mediaProjection == null) {
            Log.e(TAG, "media projection is null");
            return;
        }

        Point outSize = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(outSize);
        int displayWidth = outSize.x;
        int displayHeight = outSize.y;

        String strDateFormat = "HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(strDateFormat);

        File file = new File(getExternalFilesDir(null), String.format("scren-{%1$s}.mp4", simpleDateFormat.format(new Date()))); //录屏生成文件
        if (mScreenRecordService != null) {
            mScreenRecordService.quit();
            mScreenRecordService = null;
        }
        mScreenRecordService = new ScreenRecordService(displayWidth, displayHeight, 6000000, 1,
                mediaProjection, file.getAbsolutePath());
        mScreenRecordService.start();
    }
}
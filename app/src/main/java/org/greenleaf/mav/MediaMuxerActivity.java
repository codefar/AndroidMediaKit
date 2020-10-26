package org.greenleaf.mav;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;

import org.greenleaf.mav.encoder.MediaMuxerWrapper;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MediaMuxerActivity extends AppCompatActivity {

    private static final String TAG = "MediaMuxerActivity";
    private MediaMuxerWrapper mMediaMuxerWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_record);

        Button button = findViewById(R.id.start);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionsUtil.requestPermission(MediaMuxerActivity.this, new PermissionListener() {
                            @Override
                            public void permissionGranted(@NonNull String[] permission) {
                                if (mMediaMuxerWrapper == null) {
                                    try {
                                        mMediaMuxerWrapper = new MediaMuxerWrapper("");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    mMediaMuxerWrapper.stopRecording();
                                }
                                mMediaMuxerWrapper.startRecording();
                                Log.d(TAG, "permissionGranted: Start");
                            }

                            @Override
                            public void permissionDenied(@NonNull String[] permission) {
                            }
                        }, Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });

        Button button2 = findViewById(R.id.stop);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaMuxerWrapper != null) {
                    mMediaMuxerWrapper.stopRecording();
                }
            }
        });
    }
}
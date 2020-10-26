package org.greenleaf.mav;

import android.Manifest;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainCamera1Activity extends AppCompatActivity implements SurfaceHolder.Callback, PreviewCallback {

    private static final String TAG = "MainActivity";
    private SurfaceView surfaceview;
    private SurfaceHolder surfaceHolder;

    private Camera mCamera;
    int width = 720;
    int height = 1280;
    int framerate = 30;
    int biterate = 8500 * 1000;
    //待解码视频缓冲队列，静态成员！
    private CameraAvcEncoder avcCodec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionsUtil.requestPermission(this, new PermissionListener() {
                    @Override
                    public void permissionGranted(@NonNull String[] permission) {
                        surfaceview = (SurfaceView) findViewById(R.id.surfaceView);
                        surfaceHolder = surfaceview.getHolder();
                        surfaceHolder.addCallback(MainCamera1Activity.this);
                    }

                    @Override
                    public void permissionDenied(@NonNull String[] permission) {
                    }
                }, Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
                , Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged() called with: holder = [" + holder + "], format = [" + format + "], width = [" + width + "], height = [" + height + "]");
        initCamera1();
        //创建AvEncoder对象
        avcCodec = new CameraAvcEncoder(width, height, framerate);
        //启动编码线程
        avcCodec.startEncoderThread();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            avcCodec.stopThread();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, android.hardware.Camera camera) {
        //将当前帧图像保存在队列中
        Log.d(TAG, "onPreviewFrame() called with: data = [" + data.length + "]");
        avcCodec.enqueueCameraFrameData(data);
        camera.addCallbackBuffer(data);
    }

    private void initCamera1() {
        try {
            //获取Camera的实例
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "getBackCamera: ", e);
            return;
        }
        try {
            mCamera.setPreviewCallback(this);
            mCamera.setDisplayOrientation(90);
            Parameters parameters = mCamera.getParameters();
            Log.d(TAG, "startcamera() called with: mCamera = [" + mCamera.getParameters().getPreviewSize().height
                    + " " + mCamera.getParameters().getPreviewSize().width + "]");
            width = mCamera.getParameters().getPreviewSize().width;
            height = mCamera.getParameters().getPreviewSize().height;
            //设置预览格式
            parameters.setPreviewFormat(ImageFormat.NV21);
            //设置预览图像分辨率
            parameters.setPreviewSize(width, height);
            //配置camera参数
            mCamera.setParameters(parameters);
            //将完全初始化的SurfaceHolder传入到setPreviewDisplay(SurfaceHolder)中
            //没有surface的话，相机不会开启preview预览
            mCamera.setPreviewDisplay(surfaceHolder);
            //调用startPreview()用以更新preview的surface，必须要在拍照之前start Preview
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}
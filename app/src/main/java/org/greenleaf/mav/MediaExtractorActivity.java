package org.greenleaf.mav;

import android.Manifest;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.github.dfqin.grantor.PermissionListener;
import com.github.dfqin.grantor.PermissionsUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MediaExtractorActivity extends AppCompatActivity {

    private static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String FILE_NAME = "demo.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_extractor);
        Button button = findViewById(R.id.start);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionsUtil.requestPermission(MediaExtractorActivity.this, new PermissionListener() {
                            @Override
                            public void permissionGranted(@NonNull String[] permission) {
                                Log.d("MediaExtractorService", "permissionGranted: ");
                                MediaExtractorService mediaExtractorService = new MediaExtractorService(ROOT_DIR + "/" + FILE_NAME);
                                mediaExtractorService.start();
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

            }
        });
    }

    class MediaExtractorService extends Thread {

        private static final String TAG = "MediaExtractorService";
        public final static String AUDIO_MIME = "audio";
        public final static String VIDEO_MIME = "video";

        String sourceMediaPath;
        MediaExtractor mediaExtractor;

        public MediaExtractorService(String sourceMediaPath) {
            this.sourceMediaPath = sourceMediaPath;
            Log.d(TAG, "MediaExtractorService: " + sourceMediaPath);
        }

        @Override
        public void run() {
            super.run();
            init();
            divideMedia();
            Log.e(TAG, "FINISHED");
        }

        private boolean init() {
            boolean init = false;
            try {
                mediaExtractor = new MediaExtractor();
                mediaExtractor.setDataSource(sourceMediaPath);
//                for (int i = 0; i < trackCount; i++) {
//                    MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
//                    Log.e(TAG, i + "编号通道格式 = " + mediaFormat.getString(MediaFormat.KEY_MIME));
//                }
                init = true;
            } catch (IOException e) {
                Log.e(TAG, "init: ", e);
            }
            return init;
        }

        public void divideMedia() {
            try {
                int trackCount = mediaExtractor.getTrackCount();
                Log.e(TAG, "轨道数量 = " + trackCount);
                int videoTrackIndex = 0;//视频轨道索引
                MediaFormat videoMediaFormat = null;//视频格式
                int audioTrackIndex = 0;//音频轨道索引
                MediaFormat audioMediaFormat = null;

                for (int i = 0; i < trackCount; i++) {
                    MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
                    Log.e(TAG, i + "编号通道格式 = " + mediaFormat.getString(MediaFormat.KEY_MIME));
                    String mime = mediaFormat.getString(MediaFormat.KEY_MIME).trim();
                    if (mime.startsWith(AUDIO_MIME)) {
                        audioTrackIndex = i;
                        audioMediaFormat = mediaFormat;
                    } else if (mime.startsWith(VIDEO_MIME)) {
                        videoTrackIndex = i;
                        videoMediaFormat = mediaFormat;
                    } else {
                        Log.e(TAG, "divideMedia: Other MIME = " + mime);
                    }
                }

                /**
                 * 分离音频
                 */

                if (audioMediaFormat != null) {
                    String audioName = ROOT_DIR + "/"
                            + sourceMediaPath.substring(sourceMediaPath.lastIndexOf('/') + 1, sourceMediaPath.lastIndexOf('.'))
                            + "_audio_out.mp4";
                    Log.e(TAG, "divide audio media to file audioName:" + audioName);
                    MediaMuxer audioMediaMuxer = new MediaMuxer(audioName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    int audioTrack = audioMediaMuxer.addTrack(audioMediaFormat);
                    audioMediaMuxer.start();
                    divideToOutputAudio(mediaExtractor, audioMediaMuxer, audioMediaFormat, audioTrack, audioTrackIndex);
                    audioMediaMuxer.stop();
                    audioMediaMuxer.release();
                }

                /**
                 * 分离视频
                 */
                if (videoMediaFormat != null) {
                    String videoName = ROOT_DIR + "/"
                            + sourceMediaPath.substring(sourceMediaPath.lastIndexOf('/') + 1, sourceMediaPath.lastIndexOf('.'))
                            + "_video_out.mp4";
                    Log.e(TAG, "divide video media to file videoName:" + videoName);
                    MediaMuxer videoMediaMuxer = new MediaMuxer(videoName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    int videoTrack = videoMediaMuxer.addTrack(videoMediaFormat);
                    videoMediaMuxer.start();
                    divideToOutputVideo(mediaExtractor, videoMediaMuxer, videoMediaFormat, videoTrack, videoTrackIndex);
                    videoMediaMuxer.stop();
                    videoMediaMuxer.release();
                }
                mediaExtractor.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void divideToOutputVideo(MediaExtractor mediaExtractor, MediaMuxer mediaMuxer, MediaFormat videoMediaFormat,
                                         int videoTrack, int videoTrackIndex) {
            Log.d(TAG, "divideToOutputAudio " + videoMediaFormat.toString());
            //获取视频的输出缓存的最大大小
            int maxVideoBufferCount = videoMediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
            ByteBuffer videoByteBuffer = ByteBuffer.allocate(maxVideoBufferCount);
//            final long videoDuration = videoMediaFormat.getLong(MediaFormat.KEY_DURATION);
            mediaExtractor.selectTrack(videoTrackIndex);
//            Log.d(TAG, "divideToOutputVideo: divideToOutputVideo= " + videoSampleTime);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            bufferInfo.presentationTimeUs = 0;

            int sampleSize;
            while ((sampleSize = mediaExtractor.readSampleData(videoByteBuffer, 0)) != -1) {
//                long presentTime = bufferInfo.presentationTimeUs;
//                if (presentTime >= videoDuration) {
//                    //mediaExtractor.unselectTrack(videoTrackIndex);
//                    //Log.d(TAG, "divideToOutputVideo: presentTime >= videoDuration");
//                    //break;
//                }

                bufferInfo.offset = 0;
                bufferInfo.flags = mediaExtractor.getSampleFlags();
                bufferInfo.size = sampleSize;
                bufferInfo.presentationTimeUs = mediaExtractor.getSampleTime();
                mediaMuxer.writeSampleData(videoTrack, videoByteBuffer, bufferInfo);
                mediaExtractor.advance();
            }
            mediaExtractor.unselectTrack(videoTrackIndex);
        }

        private void divideToOutputAudio(MediaExtractor mediaExtractor, MediaMuxer mediaMuxer, MediaFormat audioMediaFormat,
                                         int audioTrack, int audioTrackIndex) {
            Log.d(TAG, "divideToOutputAudio " + audioMediaFormat.toString());
            long audioDuration = audioMediaFormat.getLong(MediaFormat.KEY_DURATION);
            mediaExtractor.selectTrack(audioTrackIndex);
            //参数为多媒体文件MediaExtractor获取到的track count的索引,选择音频轨道
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            bufferInfo.presentationTimeUs = 0;

            int maxAudioBufferCount = audioMediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);//获取音频的输出缓存的最大大小
            ByteBuffer byteBuffer = ByteBuffer.allocate(maxAudioBufferCount);

            long audioSampleSize;
            //跳过第一个 I 帧
            {
                mediaExtractor.readSampleData(byteBuffer, 0);
                if (mediaExtractor.getSampleTime() == 0) {
                    mediaExtractor.advance();
                }
                mediaExtractor.readSampleData(byteBuffer, 0);
                long firstRateSample = mediaExtractor.getSampleTime();

                mediaExtractor.advance();
                mediaExtractor.readSampleData(byteBuffer, 0);
                long secondRateSample = mediaExtractor.getSampleTime();
                audioSampleSize = Math.abs(secondRateSample - firstRateSample);
            }
            Log.d(TAG, "divideToOutputAudio: audioSampleSize= " + audioSampleSize);
            mediaExtractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);

            int sampleSize;
            while ((sampleSize = mediaExtractor.readSampleData(byteBuffer, 0)) != -1) {
                int trackIndex = mediaExtractor.getSampleTrackIndex();
                long presentationTimeUs = bufferInfo.presentationTimeUs;
                Log.i(TAG, "trackIndex:" + trackIndex + ",presentationTimeUs:" + presentationTimeUs);
                if (presentationTimeUs >= audioDuration) {
                    mediaExtractor.unselectTrack(audioTrackIndex);
                    break;
                }
                mediaExtractor.advance();
                bufferInfo.offset = 0;
                bufferInfo.size = sampleSize;
                mediaMuxer.writeSampleData(audioTrack, byteBuffer, bufferInfo);//audioTrack为通过mediaMuxer.add()获取到的
                bufferInfo.presentationTimeUs += audioSampleSize;
            }
            mediaExtractor.unselectTrack(audioTrackIndex);
        }

        private void finish(MediaExtractor mediaExtractor, MediaMuxer mediaMuxer) {
            mediaMuxer.stop();
            mediaMuxer.release();
            mediaMuxer = null;
            mediaExtractor.release();
            mediaExtractor = null;
        }
    }
}
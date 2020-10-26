package org.greenleaf.mav;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaMuxerService {

    private void doWork() throws IOException {
        MediaMuxer muxer = new MediaMuxer("temp.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        // More often, the MediaFormat will be retrieved from MediaCodec.getOutputFormat()
        // or MediaExtractor.getTrackFormat().
        MediaFormat audioFormat = null;//new MediaFormat(MediaFormat.MIMETYPE_AUDIO_AMR_NB);
        MediaFormat videoFormat = null;//MediaFormat.createVideoFormat()
        int audioTrackIndex = muxer.addTrack(audioFormat);
        int videoTrackIndex = muxer.addTrack(videoFormat);
        int bufferSize = 200000;
        ByteBuffer inputBuffer = ByteBuffer.allocate(bufferSize);
        boolean finished = false;
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        muxer.start();
        while(!finished) {
            // getInputBuffer() will fill the inputBuffer with one frame of encoded
            // sample from either MediaCodec or MediaExtractor, set isAudioSample to
            // true when the sample is audio data, set up all the fields of bufferInfo,
            // and return true if there are no more samples.

            //finished = getInputBuffer(inputBuffer, isAudioSample, bufferInfo);
            if (!finished) {
                boolean isAudioSample = false;
                int currentTrackIndex = isAudioSample ? audioTrackIndex : videoTrackIndex;
                muxer.writeSampleData(currentTrackIndex, inputBuffer, bufferInfo);
            }
        };
        muxer.stop();
        muxer.release();
    }
}

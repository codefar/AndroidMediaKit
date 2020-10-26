package org.greenleaf.mav;

import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Log;

import java.nio.ByteBuffer;

import static android.graphics.ImageFormat.NV21;

public class ImageUtil {

    private static final String TAG = "ImageUtil";
    private static final int YUV420P = 1;
    private static final int YUV420SP = 2;

    public static byte[] getBytesFromImageAsType(Image image, int type) {
        try {
            //Get the source data, if it is YUV format data planes.length = 3
            final Image.Plane[] planes = image.getPlanes();
            //Data effective width, in general, image width <= rowStride, which is also the reason for byte []. Length <= capacity
            // So we only take the width part
            int width = image.getWidth();
            int height = image.getHeight();
            Log.i(TAG, "image width = " + image.getWidth() + "; image height = " + image.getHeight());

            //This is used to fill the final YUV data, which requires 1.5 times the picture size, because the YUV ratio is 4: 1: 1
            byte[] yuvBytes = new byte[width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
            //The position to which the target array is filled
            int dstIndex = 0;
            //Temporary storage of uv data
            byte uBytes[] = new byte[width * height / 4];
            byte vBytes[] = new byte[width * height / 4];
            int uIndex = 0;
            int vIndex = 0;

            int pixelsStride, rowStride;
            for (int i = 0; i < planes.length; i++) {
                pixelsStride = planes[i].getPixelStride();
                rowStride = planes[i].getRowStride();

                ByteBuffer buffer = planes[i].getBuffer();

                //The index of the source data. The data of y is continuous in byte. The data of u is shifted to the left. It is assumed that both are even-numbered bits.
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                int srcIndex = 0;
                if (i == 0) {
                    //Take out all the valid areas of Y directly, or store them as a temporary byte, and then copy it to the next step.
                    for (int j = 0; j < height; j++) {
                        System.arraycopy(bytes, srcIndex, yuvBytes, dstIndex, width);
                        srcIndex += rowStride;
                        dstIndex += width;
                    }
                } else if (i == 1) {
                    //Take corresponding data according to pixelsStride
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            uBytes[uIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                } else if (i == 2) {
                    //Take corresponding data according to pixelsStride
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            vBytes[vIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                }
            }
            //Fill based on required result type
            switch (type) {
                case YUV420P:
                    System.arraycopy(uBytes, 0, yuvBytes, dstIndex, uBytes.length);
                    System.arraycopy(vBytes, 0, yuvBytes, dstIndex + uBytes.length, vBytes.length);
                    break;
                case YUV420SP:
                    for (int i = 0; i < vBytes.length; i++) {
                        yuvBytes[dstIndex++] = uBytes[i];
                        yuvBytes[dstIndex++] = vBytes[i];
                    }
                    break;
                case NV21:
                    for (int i = 0; i < vBytes.length; i++) {
                        yuvBytes[dstIndex++] = vBytes[i];
                        yuvBytes[dstIndex++] = uBytes[i];
                    }
                    break;
            }
            return yuvBytes;
        } catch (final Exception e) {
            if (image != null) {
                image.close();
            }
            Log.e(TAG, e.toString());
        }
        return null;
    }
}

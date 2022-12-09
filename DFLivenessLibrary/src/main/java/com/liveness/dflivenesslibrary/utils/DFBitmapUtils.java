package com.liveness.dflivenesslibrary.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DFBitmapUtils {
    public static Bitmap convertNv21ToBmp(byte[] data, int width, int height, Rect crop,
                                          int rotation, boolean isMirror) throws IOException {
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21,
                width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width,
                height), 100, baos);
        byte[] jpegBytes = baos.toByteArray();
        baos.flush();
        baos.close();
        Bitmap photoBmp = BitmapFactory.decodeByteArray(jpegBytes, 0,
                jpegBytes.length);

        Matrix matrix = new Matrix();
        matrix.setRotate(rotation);
        if (isMirror) {
            matrix.postScale(-1, 1);
        }
        int left = 0, top = 0, cropWidth = photoBmp.getWidth(), cropHeight = photoBmp.getHeight();
        if (crop != null) {
            left = crop.left;
            top = crop.top;
            cropWidth = crop.width();
            cropHeight = crop.height();
        }
        Bitmap rotated = Bitmap.createBitmap(photoBmp, left, top, cropWidth, cropHeight, matrix, false);
        if (rotated != photoBmp) {
            recyleBitmap(photoBmp);
        }
        photoBmp = rotated;
        return photoBmp;
    }

    public static void recyleBitmap(Bitmap bmp) {
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
        }
    }

    public static Bitmap convertNv21ToBmp(byte[] data, int width, int height) throws IOException {
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21,
                width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width,
                height), 100, baos);
        byte[] jpegBytes = baos.toByteArray();
        baos.flush();
        baos.close();
        return BitmapFactory.decodeByteArray(jpegBytes, 0,
                jpegBytes.length);
    }

    public static Bitmap convertNv21ToBmp(byte[] data, int width, int height, boolean landscape, int rotateDegree) throws IOException {
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21,
                width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width,
                height), 100, baos);
        byte[] jpegBytes = baos.toByteArray();
        baos.flush();
        baos.close();
        Bitmap photoBmp = BitmapFactory.decodeByteArray(jpegBytes, 0,
                jpegBytes.length);
        int degree = rotateDegree * 90;
        Matrix matrix = new Matrix();
        matrix.setRotate(degree);
        if (landscape) {
            matrix.postScale(-1, 1);
        }
        Bitmap rotated = Bitmap.createBitmap(photoBmp, 0, 0, photoBmp.getWidth(), photoBmp.getHeight(), matrix, false);
        if (rotated != photoBmp) {
            recyleBitmap(photoBmp);
        }
        photoBmp = rotated;
        return photoBmp;
    }

    public static byte[] convertBmpToJpeg(Bitmap result) {
        if (result == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        result.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] jpeg = byteArrayOutputStream.toByteArray();
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jpeg;
    }

    public static Bitmap cropResultBitmap(Bitmap cropBitmap, int targetW, int targetH, RectF ratioF) {
        Rect rect = new Rect();
        float ratio = 0.05f;
        float width_margin = 10;//rectF.width() * ratio;
        float height_margin = 10;//rectF.height() * ratio;
        rect.left = (int) (targetH * ratioF.left - width_margin);
        rect.top = (int) (targetW * ratioF.top - height_margin);
        rect.right = (int) (targetH * ratioF.right + width_margin);
        rect.bottom = (int) (targetW * ratioF.bottom + height_margin);
        if(rect.left < 0 || rect.top < 0 || rect.bottom > cropBitmap.getHeight() || rect.right > cropBitmap.getWidth()) {
            return null;
        }
        return Bitmap.createBitmap(cropBitmap, rect.left, rect.top, rect.width(), rect.height());
    }
}

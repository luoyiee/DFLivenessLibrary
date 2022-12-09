package com.liveness.dflivenesslibrary.liveness.util;


import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.dfsdk.liveness.DFLivenessSDK;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class LivenessUtils {
    private static final String TAG = "LivenessUtils";

    public static final boolean DEBUG = false;

    /*
    * Return whether the system is root or not.
    */
    public static boolean isRootSystem() {
        int systemRootState = 0; // not root
        File f;
        final String kSuSearchPaths[] = {"/system/bin/", "/system/xbin/",
                "/system/sbin/", "/sbin/", "/vendor/bin/"};
        try {
            for (int i = 0; i < kSuSearchPaths.length; i++) {
                f = new File(kSuSearchPaths[i] + "su");
                if (f.exists()) {
                    systemRootState = 1; //root
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return systemRootState == 1;
    }

    /**
     * From the byte array, generate the file
     */
    public static void saveFile(byte[] bfile, String filePath, String fileName) {
        if (bfile == null) {
            return;
        }
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory()) {
                boolean mkdirs = dir.mkdirs();
            }
            File file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static DFLivenessSDK.DFLivenessMotion[] getMctionOrder(String input) {
        if (input == null) {
            return null;
        }
        String[] splitStrings = input.split("\\s+");
        DFLivenessSDK.DFLivenessMotion[] detectList = new DFLivenessSDK.DFLivenessMotion[splitStrings.length];
        for (int i = 0; i < splitStrings.length; i++) {
            if (splitStrings[i].equalsIgnoreCase(Constants.BLINK)) {
                detectList[i] = DFLivenessSDK.DFLivenessMotion.BLINK;
            } else if (splitStrings[i].equalsIgnoreCase(Constants.NOD)) {
                detectList[i] = DFLivenessSDK.DFLivenessMotion.NOD;
            } else if (splitStrings[i].equalsIgnoreCase(Constants.MOUTH)) {
                detectList[i] = DFLivenessSDK.DFLivenessMotion.MOUTH;
            } else if (splitStrings[i].equalsIgnoreCase(Constants.YAW)) {
                detectList[i] = DFLivenessSDK.DFLivenessMotion.YAW;
            } else if (splitStrings[i].equalsIgnoreCase(Constants.HOLD_STILL)) {
                detectList[i] = DFLivenessSDK.DFLivenessMotion.HOLD_STILL;
            }
        }
        return detectList;
    }

    public static String[] getDetectActionOrder(String input) {
        if (input == null) {
            return null;
        }
        return input.split("\\s+");
    }


    public static void deleteFiles(String folderPath) {
        File dir = new File(folderPath);
        if (!dir.exists() || !dir.isDirectory() || dir.listFiles() == null)
            return;
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        boolean delete = file.delete();
                    }
                }
            }
        }
    }

    public static void logI(Object... logValue) {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder();
            if (logValue != null) {
                for (Object value : logValue) {
                    if (value != null) {
                        sb.append("*")
                                .append(value.toString())
                                .append("*");
                    }
                }
            }
            Log.i(TAG, "logI*" + sb.toString());
        }
    }

    public static int[] getScreenSize(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowManager = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            display.getRealMetrics(dm);
        }
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        LivenessUtils.logI(TAG, "getScreenSize", "width", width);
        LivenessUtils.logI(TAG, "getScreenSize", "height", height);
        return new int[]{width, height};
    }
}

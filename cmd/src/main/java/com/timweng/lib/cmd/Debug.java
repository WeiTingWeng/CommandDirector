
package com.timweng.lib.cmd;

import android.util.Log;

public class Debug {
    private final static String TAG = "CommandDirector";
    public static boolean sIsShowLog = true;

    public static void logD(String tag, String msg) {
        if (sIsShowLog) {
            Log.d(TAG, tag + "." + msg);
        }
    }

    public static void logE(String tag, String msg) {
        if (sIsShowLog) {
            Log.e(TAG, tag + "." + msg);
        }
    }

    public static void logE(String tag, String msg, Throwable tr) {
        if (sIsShowLog) {
            Log.e(TAG, tag + "." + msg, tr);
        }
    }

    public static void logI(String tag, String msg) {
        if (sIsShowLog) {
            Log.i(TAG, tag + "." + msg);
        }
    }

    public static void logW(String tag, String msg) {
        if (sIsShowLog) {
            Log.w(TAG, tag + "." + msg);
        }
    }

    public static void logW(String tag, String msg, Throwable tr) {
        if (sIsShowLog) {
            Log.w(TAG, tag + "." + msg, tr);
        }
    }
}

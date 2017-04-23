
package com.timweng.lib.cmd;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class Debug {
    private static String sTag = "CommandDirector";
    public static boolean sIsShowLog = true;
    public static boolean sIsShowToast = true;

    private boolean mIsInit = false;
    private Activity mActivity = null;
    private Toast mToast;

    private static Debug ourInstance = new Debug();

    public static Debug getInstance() {
        return ourInstance;
    }

    private Debug() {
    }

    public void init(Activity activity, String tag) {
        mActivity = activity;
        mToast = Toast.makeText(mActivity, "", Toast.LENGTH_SHORT);
        sTag = tag;
        mIsInit = true;
    }

    public void release() {
        mIsInit = false;
        mActivity = null;
        mToast = null;
    }

    public void setIsShowLog(boolean isShowLog) {
        sIsShowLog = isShowLog;
    }

    public void setIsShowToast(boolean isShowToast) {
        sIsShowToast = isShowToast;
    }

    public void showToast(final String string) {
        if (mIsInit && sIsShowToast && mToast != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mToast != null) {
                        mToast.setText(string);
                        mToast.show();
                    }
                }
            });
        }
    }

    public static void logD(String tag, String msg) {
        if (sIsShowLog) {
            Log.d(sTag, tag + "." + msg);
        }
    }

    public static void logE(String tag, String msg) {
        if (sIsShowLog) {
            Log.e(sTag, tag + "." + msg);
        }
    }

    public static void logE(String tag, String msg, Throwable tr) {
        if (sIsShowLog) {
            Log.e(sTag, tag + "." + msg, tr);
        }
    }

    public static void logI(String tag, String msg) {
        if (sIsShowLog) {
            Log.i(sTag, tag + "." + msg);
        }
    }

    public static void logW(String tag, String msg) {
        if (sIsShowLog) {
            Log.w(sTag, tag + "." + msg);
        }
    }

    public static void logW(String tag, String msg, Throwable tr) {
        if (sIsShowLog) {
            Log.w(sTag, tag + "." + msg, tr);
        }
    }
}

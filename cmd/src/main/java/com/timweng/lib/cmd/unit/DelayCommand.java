
package com.timweng.lib.cmd.unit;

import android.os.Bundle;
import android.os.Handler;

import com.timweng.lib.cmd.Command;
import com.timweng.lib.cmd.Debug;

/**
 * An Action for delay
 */
public class DelayCommand extends Command {

    private static final String TAG = "DelayAction";

    private long mDelayDuring = 1000;
    private long mRemainDuring = 0;
    private long mStartTime = 0;

    public DelayCommand(long during) {
        setDelayDuring(during);
    }

    public DelayCommand setDelayDuring(long during) {
        if (during > 0) {
            mDelayDuring = during;
        } else {
            Debug.logD(TAG, "mDelayDuring must larger than 0");
        }
        return this;
    }

    @Override
    public boolean start(Handler handler, Bundle bundle) {
        if (!super.start(handler, bundle)) {
            return false;
        }
        mRemainDuring = mDelayDuring;
        mStartTime = System.currentTimeMillis();
        mHandler.postDelayed(mDelayRunnable, mRemainDuring);
        return true;
    }

    @Override
    public boolean stop() {
        if (!super.stop()) {
            return false;
        }
        mHandler.removeCallbacks(mDelayRunnable);
        mRemainDuring = 0;
        return true;
    }

    @Override
    public boolean pause() {
        if (!super.pause()) {
            return false;
        }
        mHandler.removeCallbacks(mDelayRunnable);
        long runDuring = System.currentTimeMillis() - mStartTime;
        mRemainDuring -= runDuring;
        Debug.logD(TAG, "start.mRemainDuring = " + mRemainDuring);
        return true;
    }

    @Override
    public boolean resume() {
        if (!super.resume()) {
            return false;
        }
        Debug.logD(TAG, "resume.mRemainDuring = " + mRemainDuring);

        if (mRemainDuring > 0) {
            mStartTime = System.currentTimeMillis();
            mHandler.postDelayed(mDelayRunnable, mRemainDuring);
        } else {
            mHandler.post(mDelayRunnable);
        }
        return true;
    }

    private Runnable mDelayRunnable = new Runnable() {

        @Override
        public void run() {
            mRemainDuring = 0;
            onComplete(null);
        }
    };

}


package com.timweng.lib.cmd;

import android.os.Bundle;
import android.os.Handler;

/**
 * The base Command, it's a abstract class, if user want to create customized Command, need to extend this class
 */
public abstract class Command {

    private static final String TAG = "Command";
    private static volatile int sCount = 0; // for Id

    /**
     * Used by CommandDirector, the callback that can get the Command playing update
     */
    public static interface OnCommandUpdateListener {
        public void onComplete(Command callCommand, Bundle bundle);

        public void onError(Command callCommand, Bundle bundle);
    }

    private int mId = -1;
    protected int mParentId = -1;

    private volatile boolean mIsProcessing = false;
    private volatile boolean mIsPause = false;

    protected OnCommandUpdateListener mOnCommandUpdateListener;
    protected Handler mHandler;

    protected Command mNextCommand = null;

    /**
     * Get the unique Command ID
     *
     * @return unique Command ID
     */
    public int getId() {
        if (mId < 0) {
            mId = sCount;
            sCount++;
        }
        return mId;
    }

    /**
     * Controlled by CommandDirector, DO NOT use this function
     *
     * @param id parent ID
     */
    public void setParentId(int id) {
        mParentId = id;
    }

    /**
     * Controlled by CommandDirector, DO NOT use this function
     *
     * @return parent ID
     */
    public int getParentId() {
        return mParentId;
    }

    public Command setListener(OnCommandUpdateListener listener) {
        mOnCommandUpdateListener = listener;
        return this;
    }

    /**
     * Get the Command is processing or not
     *
     * @return is processing or not
     */
    public boolean isProcessing() {
        return mIsProcessing;
    }

    /**
     * Get Command is pause or not
     *
     * @return is pause or not
     */
    public boolean isPause() {
        return mIsPause;
    }

    /**
     * Controlled by CommandDirector, start this Command
     *
     * @param handler handler for worker thread
     * @param bundle  the bundle from last Command, need to check null or not
     * @return can start or not
     */
    public boolean start(Handler handler, Bundle bundle) {
        mHandler = handler;
        if (mIsProcessing) {
            Debug.logD(TAG, "start() failed: " + this);
            return false;
        } else {
            mIsProcessing = true;
            mIsPause = false;
            return true;
        }
    }

    /**
     * Controlled by CommandDirector, stop this Command
     *
     * @return can stop or not
     */
    public boolean stop() {
        if (!mIsProcessing) {
            Debug.logD(TAG, "stop() failed");
            return false;
        } else {
            mIsProcessing = false;
            mIsPause = false;
            return true;
        }
    }

    /**
     * Controlled by CommandDirector, pause this Command
     *
     * @return can pause or not
     */
    public boolean pause() {
        if (!mIsProcessing || mIsPause) {
            Debug.logD(TAG, "pause() failed");
            return false;
        } else {
            mIsProcessing = false;
            mIsPause = true;
            return true;
        }
    }

    /**
     * Controlled by CommandDirector, resume this Command
     *
     * @return can resume or not
     */
    public boolean resume() {
        if (mIsProcessing || !mIsPause) {
            Debug.logD(TAG, "resume() failed");
            return false;
        } else {
            mIsProcessing = true;
            mIsPause = false;
            return true;
        }
    }

    /**
     * Set the next Command, when this Command completed the next Command will start
     *
     * @param nextCommand the next Command
     * @return the reference of the nextCommand
     */
    public Command setNext(Command nextCommand) {
        mNextCommand = nextCommand;
        return mNextCommand;
    }

    /**
     * Get the next Command
     *
     * @return the next Command, null means there is no next, CommandDirector calls onComplete
     */
    public Command getNext() {
        return mNextCommand;
    }


    /**
     * Has next Command or not
     *
     * @return has next Command or not
     */
    public boolean hasNext() {
        return mNextCommand != null;
    }

    protected void onComplete(Bundle bundle) {
        mIsProcessing = false;
        mIsPause = false;
        if (mOnCommandUpdateListener != null) {
            mOnCommandUpdateListener.onComplete(this, bundle);
        }
    }

    protected void onError(Bundle bundle) {
        mIsProcessing = false;
        mIsPause = false;
        if (mOnCommandUpdateListener != null) {
            mOnCommandUpdateListener.onError(this, bundle);
        }
    }
}


package com.timweng.lib.cmd;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.timweng.lib.cmd.unit.SpawnCommand;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

/**
 * The CommandDirector is for handling Commands, user must control Commands by this player.
 */
public class CommandDirector {
    private static final String TAG = "CommandDirector";

    /**
     * The callback that can get the Commands playing update
     */
    public static interface OnDirectorUpdateListener {
        /**
         * Call when CommandDirector complete the Command successfully continue to the next
         *
         * @param lastCommand the last Command instance
         * @param nextCommand the next Command instance
         * @param lastBundle  the bundle form the last Command
         */
        public void onNext(Command lastCommand, Command nextCommand, Bundle lastBundle);

        /**
         * Call when CommandDirector complete all the Commands
         *
         * @param lastCommand the last Command instance
         * @param lastBundle  the bundle form the last Command
         */
        public void onComplete(Command lastCommand, Bundle lastBundle);

        /**
         * Call when Command get error
         *
         * @param lastCommand the Command that get error
         * @param nextCommand the next Command instance
         * @param errorBundle the error bundle
         */
        public void onError(Command lastCommand, Command nextCommand, Bundle errorBundle);
    }

    protected volatile boolean mIsProcessing = false;
    protected volatile boolean mIsPause = false;

    private Handler mMainHandler;
    private HandlerThread mThread;
    private Handler mHandler;

    private Vector<Command> mCurCommandVector = new Vector<Command>();
    private Map<Integer, SpawnCommand.SpawnData> mSpawnMap = new HashMap<Integer, SpawnCommand.SpawnData>();

    private HashSet<OnDirectorUpdateListener> mListenerSet = new HashSet<OnDirectorUpdateListener>();
    private Map<Command, Bundle> mStartBundleMap = new HashMap<>();

    private final Object mControlLock = new Object();

    public CommandDirector() {
        synchronized (mControlLock) {
            mMainHandler = new Handler(Looper.getMainLooper());
            mThread = new HandlerThread("BehaviorManager.mThread");
            mThread.start();
            mHandler = new Handler(mThread.getLooper());
        }
    }

    /**
     * Release the CommandDirector when user no longer using it, if user want to using it, need to init() it again.
     */
    public void release() {
        synchronized (mControlLock) {
            stopAllCurCommands();
            mThread.quit();
            mMainHandler = null;
            mListenerSet.clear();
        }
    }

    /**
     * Register the OnDirectorListener
     *
     * @param listener the listener user want to register
     * @return true if this set did not already contain the specified element
     */
    public boolean registerListener(OnDirectorUpdateListener listener) {
        boolean isOk;
        synchronized (mListenerSet) {
            isOk = mListenerSet.add(listener);
        }
        return isOk;
    }

    /**
     * Unregister the OnDirectorListener
     *
     * @param listener the listener user want to unregister
     * @return true if this set contained the specified element
     */
    public boolean unregisterListener(OnDirectorUpdateListener listener) {
        boolean isOk;
        synchronized (mListenerSet) {
            isOk = mListenerSet.remove(listener);
        }
        return isOk;
    }

    /**
     * Get the player is processing commands or not
     *
     * @return is processing commands or not
     */
    public boolean isProcessing() {
        return mIsProcessing;
    }

    /**
     * Start the Command
     *
     * @param command the Command you want start
     * @return true if the Command can start, if player is processing return false
     */
    public boolean start(Command command) {
        synchronized (mControlLock) {
            if (mIsProcessing) {
                Debug.logD(TAG, "start() failed: mIsProcessing = " + mIsProcessing);
                return false;
            }
            mIsProcessing = true;
            mIsPause = false;
            mStartBundleMap.clear();
            return startNext(command, null);
        }
    }

    /**
     * Stop the CommandDirector
     *
     * @return true if the CommandDirector can stop, if player is not processing return false
     */
    public boolean stop() {
        synchronized (mControlLock) {
            if (!mIsProcessing) {
                Debug.logD(TAG, "stop() failed: mIsProcessing = " + mIsProcessing);
                return false;
            }
            Debug.logD(TAG, "stop() successful, mCurCommandVector.size() = "
                    + mCurCommandVector.size());
            stopAllCurCommands();
            mIsProcessing = false;
            mIsPause = false;
            mStartBundleMap.clear();
            return true;
        }
    }

    public boolean pause() {
        synchronized (mControlLock) {
            if (!mIsProcessing || mIsPause) {
                Debug.logD(TAG, "pause() failed");
                return false;
            }
            for (int i = 0; i < mCurCommandVector.size(); i++) {
                Command cmd = mCurCommandVector.get(i);
                if (cmd != null) {
                    cmd.pause();
                }
            }
            mIsProcessing = true;
            mIsPause = true;
            return true;
        }
    }

    public boolean resume() {
        synchronized (mControlLock) {
            if (!mIsProcessing || !mIsPause) {
                Debug.logD(TAG, "resume() failed");
                return false;
            }
            for (int i = 0; i < mCurCommandVector.size(); i++) {
                Command cmd = mCurCommandVector.get(i);
                if (cmd != null) {
                    if (cmd.isPause()) {
                        cmd.resume();
                    } else {
                        cmd.start(mHandler, mStartBundleMap.get(cmd));
                    }
                }
            }
            mStartBundleMap.clear();
            mIsProcessing = true;
            mIsPause = false;
            return true;
        }
    }

    private void stopAllCurCommands() {
        for (int i = 0; i < mCurCommandVector.size(); i++) {
            Command command = mCurCommandVector.get(i);
            if (command != null) {
                command.stop();
            }
        }
        mCurCommandVector.clear();
        synchronized (mSpawnMap) {
            mSpawnMap.clear();
        }
    }

    private boolean startNext(Command command, Bundle bundle) {
        if (command == null) {
            Debug.logD(TAG, "startNext() failed");
            return false;
        }
        if (command instanceof SpawnCommand) {
            mCurCommandVector.add(command);
            command.setListener(mOnCommandUpdateListener);

            Command[] aArray = null;
            synchronized (mSpawnMap) {
                SpawnCommand sa = (SpawnCommand) command;
                SpawnCommand.SpawnData spawnData = sa.genSpawnData();
                mSpawnMap.put(sa.getId(), spawnData);
                aArray = spawnData.genChildrenArray();
            }
            if (aArray != null && aArray.length > 0) {
                for (Command a : aArray) {
                    if (command instanceof SpawnCommand) {
                        startNext(a, bundle);
                    } else {
                        mCurCommandVector.add(a);
                        a.setListener(mOnCommandUpdateListener);
                        if (mIsPause) {
                            mStartBundleMap.put(a, bundle);
                        } else {
                            a.start(mHandler, bundle);
                        }
                    }
                }
            } else {
                startNext(command.getNext(), null);
            }
        } else {
            mCurCommandVector.add(command);
            command.setListener(mOnCommandUpdateListener);
            if (!mIsProcessing) {
                return false;
            } else if (mIsPause) {
                mStartBundleMap.put(command, bundle);
                return false;
            } else {
                command.start(mHandler, bundle);
            }
        }

        Debug.logD(TAG, "startNext() successful: " + command.toString());
        return true;
    }

    private Command.OnCommandUpdateListener mOnCommandUpdateListener = new Command.OnCommandUpdateListener() {

        @Override
        public void onComplete(final Command callCommand, final Bundle bundle) {
            Debug.logD(TAG, "onComplete");
            mMainHandler.post(new OnDoneRunnable(callCommand, bundle, false));
        }

        @Override
        public void onError(final Command callCommand, final Bundle bundle) {
            Debug.logD(TAG, "onError");
            mMainHandler.post(new OnDoneRunnable(callCommand, bundle, true));
        }
    };

    private class OnDoneRunnable implements Runnable {

        private Command mCallCommand;
        private Bundle mBundle;
        private boolean mIsError;

        public OnDoneRunnable(Command callCommand, Bundle bundle, boolean isError) {
            this.mCallCommand = callCommand;
            this.mBundle = bundle;
            mIsError = isError;
        }

        public void run() {
            Debug.logD(TAG, "OnDoneRunnable.mCallCommand=" + mCallCommand.toString());
            Debug.logD(TAG, "OnDoneRunnable.mNextCommand=" + mCallCommand.getNext());

            SpawnCommand doneSpawnCommand = null;

            mCurCommandVector.remove(mCallCommand);
            if (mCallCommand.getParentId() < 0) {
                Debug.logD(TAG, "OnDoneRunnable.normal Command");
                if (mCallCommand.hasNext()) {
                    if (mIsError) {
                        CommandDirector.this.onError(mCallCommand, mCallCommand.getNext(), mBundle);
                    }
                    CommandDirector.this.onNext(mCallCommand, mCallCommand.getNext(), mBundle);
                    startNext(mCallCommand.getNext(), mBundle);
                }
            } else {
                Debug.logD(TAG, "OnDoneRunnable.spawn Command");
                synchronized (mSpawnMap) {
                    Debug.logD(TAG, "OnDoneRunnable.mSpawnMap.size=" + mSpawnMap.size());

                    SpawnCommand.SpawnData spawnData = mSpawnMap.get(mCallCommand.getParentId());
                    Debug.logD(TAG, "OnDoneRunnable.spawnData.id=" + spawnData.getCommand().getId());

                    if (spawnData != null) {
                        if (mCallCommand.hasNext()) {
                            Command nextCommand = mCallCommand.getNext();
                            nextCommand.setParentId(mCallCommand.getParentId());
                            Vector<Command> cVector = spawnData.getChildren();
                            cVector.set(cVector.indexOf(mCallCommand), nextCommand);
                            if (mIsError) {
                                CommandDirector.this.onError(mCallCommand, nextCommand, mBundle);
                            }
                            CommandDirector.this.onNext(mCallCommand, nextCommand, mBundle);
                            startNext(nextCommand, mBundle);
                        } else {
                            Vector<Command> cVector = spawnData.getChildren();
                            SpawnCommand parentAct = spawnData.getCommand();
                            switch (spawnData.getCompleteType()) {
                                case ONE_DONE:
                                    cVector.remove(mCallCommand);
                                    for (Command command : cVector) {
                                        command.stop();
                                        mCurCommandVector.remove(command);
                                    }
                                    if (parentAct.hasNext()) {
                                        int perId = parentAct.getParentId();
                                        Command nextAct = parentAct.getNext();
                                        nextAct.setParentId(perId);
                                    }
                                    doneSpawnCommand = parentAct;
                                    mSpawnMap.remove(mCallCommand.getParentId());
                                    break;
                                case ALL_DONE:
                                    if (cVector.size() <= 1) {
                                        cVector.remove(mCallCommand);
                                        if (parentAct.hasNext()) {
                                            int perId = parentAct.getParentId();
                                            Command nextAct = parentAct.getNext();
                                            nextAct.setParentId(perId);
                                        }
                                        doneSpawnCommand = parentAct;
                                        mSpawnMap.remove(mCallCommand.getParentId());
                                    } else {
                                        cVector.remove(mCallCommand);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }

            Debug.logD(TAG, "OnDoneRunnable.mCurCommandVector.size()=" + mCurCommandVector.size());

            if (mCurCommandVector.isEmpty()) {
                mIsProcessing = false;
                CommandDirector.this.onComplete(mCallCommand, mBundle);
            }

            if (doneSpawnCommand != null) {
                mMainHandler.post(new OnDoneRunnable(doneSpawnCommand, mBundle, mIsError));
            }
        }
    }

    private Object[] collectListener() {
        Object[] listeners = null;
        synchronized (mListenerSet) {
            if (mListenerSet.size() > 0) {
                listeners = mListenerSet.toArray();
            }
        }
        return listeners;
    }

    private void onNext(final Command last, final Command next, final Bundle lastBundle) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                Object[] list = collectListener();
                if (list != null) {
                    for (int i = 0; i < list.length; i++) {
                        ((OnDirectorUpdateListener) list[i]).onNext(last, next, lastBundle);
                    }
                }
            }
        });
    }

    private void onComplete(final Command last, final Bundle lastBundle) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                Object[] list = collectListener();
                if (list != null) {
                    for (int i = 0; i < list.length; i++) {
                        ((OnDirectorUpdateListener) list[i]).onComplete(last, lastBundle);
                    }
                }
            }
        });

    }

    private void onError(final Command last, final Command next, final Bundle errorBundle) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                Object[] list = collectListener();
                if (list != null) {
                    for (int i = 0; i < list.length; i++) {
                        ((OnDirectorUpdateListener) list[i]).onError(last, next, errorBundle);
                    }
                }
            }
        });
    }
}

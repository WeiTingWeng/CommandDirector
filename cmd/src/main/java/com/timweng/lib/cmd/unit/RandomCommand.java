
package com.timweng.lib.cmd.unit;

import android.os.Bundle;
import android.os.Handler;

import com.timweng.lib.cmd.Command;
import com.timweng.lib.cmd.Debug;

/**
 * An Command for select next Actions randomly
 */
public class RandomCommand extends Command {
    private static final String TAG = "RandomAction";

    private Command[] mCommands;

    public RandomCommand(Command... commands) {
        mCommands = commands;
    }

    public RandomCommand setCommands(Command... commands) {
        mCommands = commands;
        return this;
    }

    @Override
    public boolean start(Handler handler, Bundle bundle) {
        if (!super.start(handler, bundle)) {
            return false;
        }
        if (mCommands == null || mCommands.length == 0) {
            Debug.logD(TAG, "Actions can not be null or empty");
        } else {
            setNext(mCommands[(int) (Math.random() * mCommands.length)]);
        }
        onComplete(null);
        return true;
    }
}

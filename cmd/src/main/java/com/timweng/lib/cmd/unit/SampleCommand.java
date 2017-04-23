
package com.timweng.lib.cmd.unit;

import android.os.Bundle;
import android.os.Handler;

import com.timweng.lib.cmd.Command;

public abstract class SampleCommand extends Command {

    // This is sample for Action, do not use it in real usage

    @Override
    public boolean start(Handler handler, Bundle bundle) {
        if (!super.start(handler, bundle)) {
            return false;
        }
        // TODO: Do function there
        return true;
    }

    @Override
    public boolean stop() {
        if (!super.stop()) {
            return false;
        }
        // TODO: Do function there
        return true;
    }

    @Override
    public boolean pause() {
        if (!super.pause()) {
            return false;
        }
        // TODO: Do function there
        return true;
    }

    @Override
    public boolean resume() {
        if (!super.resume()) {
            return false;
        }
        // TODO: Do function there
        return true;
    }

    // Call onComplete(Bundle bundle) when action is completed
    // Call onError(Bundle bundle) when action has error
}

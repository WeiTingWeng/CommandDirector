package com.timweng.lib.cmd.unit;

import android.os.Bundle;
import android.os.Handler;

import com.timweng.lib.cmd.Command;

/**
 * Created by Tim on 10/2/17.
 */

public abstract class FunctionCommand extends Command {

    @Override
    public boolean start(Handler handler, Bundle bundle) {
        if (!super.start(handler, bundle)) {
            return false;
        }
        Bundle outputBundle = function(bundle);
        onComplete(outputBundle);
        return true;
    }

    public abstract Bundle function(Bundle inputBundle);
}

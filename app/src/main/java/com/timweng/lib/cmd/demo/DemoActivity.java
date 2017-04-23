package com.timweng.lib.cmd.demo;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.timweng.lib.cmd.Command;
import com.timweng.lib.cmd.CommandDirector;
import com.timweng.lib.cmd.unit.DelayCommand;

public class DemoActivity extends AppCompatActivity {

    private CommandDirector mCommandDirector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        mCommandDirector = new CommandDirector();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Command cmd0 = new TestCmd(0);
        Command cmd1 = new DelayCommand(1000);
        Command cmd2 = new TestCmd(1);
        Command cmd3 = new DelayCommand(1000);
        Command cmd4 = new TestCmd(2);
        Command cmd5 = new DelayCommand(1000);
        Command cmd6 = new TestCmd(3);

        cmd0.setNext(cmd1).setNext(cmd2).setNext(cmd3).setNext(cmd4).setNext(cmd5).setNext(cmd6);
        mCommandDirector.start(cmd0);
    }

    @Override
    protected void onDestroy() {
        mCommandDirector.release();
        super.onDestroy();
    }

    private class TestCmd extends Command {

        private int mTestNumber = -1;

        public TestCmd(int testNumber) {
            mTestNumber = testNumber;
        }

        @Override
        public boolean start(Handler handler, Bundle bundle) {
            if (!super.start(handler, bundle)) {
                return false;
            }
            // TODO: Do function there
            Log.d("timw", "mTestNumber = " + mTestNumber);
            onComplete(null);
            return true;
        }
    }
}

package com.chtj.keepalive.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * DO NOT do anything in this Receiver!<br/>
 * <p>
 * Created by Mars on 12/24/15.
 */
public class CustomizeReceiver1 extends BroadcastReceiver {
    private static final String TAG = "Receiver1";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Receiver1");
    }
}

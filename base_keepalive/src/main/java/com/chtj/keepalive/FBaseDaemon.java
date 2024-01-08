/*
 * Original Copyright 2015 Mars Kwok
 * Modified work Copyright (c) 2020, weishu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chtj.keepalive;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.util.Log;

import com.chtj.keepalive.impl.IDaemonStrategy;
import com.chtj.keepalive.receiver.CustomizeReceiver1;
import com.chtj.keepalive.receiver.CustomizeReceiver2;
import com.chtj.keepalive.service.CustomizeService2;
import com.chtj.keepalive.service.FKeepAliveLowService;
import com.chtj.keepalive.service.FKeepAliveHighService;
import com.chtj.keepalive.service.GuardService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FBaseDaemon {
    private static final String TAG = "FBaseDaemon";
    private DaemonConfigurations mConfigurations;
    private final String DAEMON_PERMITTING_SP_FILENAME = "d_permit";
    private final String DAEMON_PERMITTING_SP_KEY = "permitted";
    private BufferedReader mBufferedReader;

    private FBaseDaemon(DaemonConfigurations configurations) {
        this.mConfigurations = configurations;
    }

    public static void init(Context base) {
        DaemonConfigurations configurations;
        String packageName = base.getPackageName();
        int sdkInt = Build.VERSION.SDK_INT;
        boolean isHigh=sdkInt >= 24;
        if (isHigh) {
            Reflection.unseal(base);
            configurations = new DaemonConfigurations(new DaemonConfigurations.LeoricConfig(packageName + ":resident", FKeepAliveHighService.class.getCanonicalName()), new DaemonConfigurations.LeoricConfig("android.media", GuardService.class.getCanonicalName()));
        } else {
            configurations = new DaemonConfigurations(new DaemonConfigurations.LeoricConfig(packageName + ":processone", FKeepAliveLowService.class.getCanonicalName(), CustomizeReceiver1.class.getCanonicalName()), new DaemonConfigurations.LeoricConfig(packageName + ":processtwo", CustomizeService2.class.getCanonicalName(), CustomizeReceiver2.class.getCanonicalName()));
        }
        FBaseDaemon client = new FBaseDaemon(configurations);
        client.initDaemon(base, isHigh);
    }


    private void initDaemon(Context base, boolean isHigh) {
        if (!isDaemonPermitting(base) || mConfigurations == null) {
            return;
        }
        String processName = getProcessName();
        String packageName = base.getPackageName();
        if (processName.startsWith(mConfigurations.PERSISTENT_CONFIG.processName)) {
            IDaemonStrategy.Fetcher.fetchStrategy().onPersistentCreate(base, mConfigurations);
        } else if (processName.startsWith(mConfigurations.DAEMON_ASSISTANT_CONFIG.processName)) {
            IDaemonStrategy.Fetcher.fetchStrategy().onDaemonAssistantCreate(base, mConfigurations);
        } else if (processName.startsWith(packageName)) {
            IDaemonStrategy.Fetcher.fetchStrategy().onInit(base);
        }
        releaseIO();
        Intent intent=new Intent(base, isHigh ? FKeepAliveHighService.class : FKeepAliveLowService.class);
        base.startService(intent);
    }


    private String getProcessName() {
        try {
            int sdk = Build.VERSION.SDK_INT;
            File file = null;
            if (sdk >= 24) {
                file = new File("/proc/self/cmdline");
            } else {
                file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            }
            mBufferedReader = new BufferedReader(new FileReader(file));
            return mBufferedReader.readLine().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void releaseIO() {
        if (mBufferedReader != null) {
            try {
                mBufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mBufferedReader = null;
        }
    }

    private boolean isDaemonPermitting(Context context) {
        SharedPreferences sp = context.getSharedPreferences(DAEMON_PERMITTING_SP_FILENAME, Context.MODE_PRIVATE);
        return sp.getBoolean(DAEMON_PERMITTING_SP_KEY, true);
    }

    protected boolean setDaemonPermiiting(Context context, boolean isPermitting) {
        SharedPreferences sp = context.getSharedPreferences(DAEMON_PERMITTING_SP_FILENAME, Context.MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putBoolean(DAEMON_PERMITTING_SP_KEY, isPermitting);
        return editor.commit();
    }

}

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

package com.chtj.keepalive.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.chtj.keepalive.FileCommonTools;
import com.chtj.keepalive.IKeepAliveListener;
import com.chtj.keepalive.IKeepAliveService;
import com.chtj.keepalive.entity.CommonValue;
import com.chtj.keepalive.entity.KeepAliveData;

import java.io.File;
import java.util.List;

/**
 * 要使用跨进程的服务 必须设置 Service
 * android:exported="true"
 * android:enabled="true"
 * 使外部可以访问
 */
public class FKeepAliveLowService extends Service {
    private static final String TAG = FKeepAliveLowService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * 多进程之间添加保活的Activity或者Service
     */
    IKeepAliveService.Stub iKeepAliveService = new IKeepAliveService.Stub() {
        @Override
        public boolean addKeepAliveInfo(KeepAliveData info, IKeepAliveListener listener) throws RemoteException {
            //Log.d(TAG, "addKeepLiveInfo:>=" + info.toString());
            if (info.getType() == FKeepAliveTools.TYPE_ACTIVITY) {
                CommonValue commonValue = FKeepAliveTools.addActivity(info);
                if (commonValue == CommonValue.EXEU_COMPLETE) {
                    listener.onSuccess();
                    return true;
                } else {
                    listener.onError(commonValue.getRemarks());
                    return false;
                }
            } else {
                CommonValue commonValue = FKeepAliveTools.addService(info);
                if (commonValue == CommonValue.EXEU_COMPLETE) {
                    listener.onSuccess();
                    return true;
                } else {
                    listener.onError(commonValue.getRemarks());
                    return false;
                }
            }
        }

        @Override
        public List<KeepAliveData> getKeepLiveInfo() throws RemoteException {
            return FKeepAliveTools.getKeepLive();
        }

        @Override
        public boolean clearAllKeepAliveInfo() throws RemoteException {
            return FileCommonTools.delete();
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        //Log.d(TAG, "onCreate: ");
        FKeepAlivePublicTools.startKeepAlive(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iKeepAliveService;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d(TAG, "onDestroy: ");
        FKeepAlivePublicTools.stopKeepAlive();
    }
}

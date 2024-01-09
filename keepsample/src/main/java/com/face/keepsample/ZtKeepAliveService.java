package com.face.keepsample;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.chtj.keepalive.entity.KeepAliveData;
import com.chtj.keepalive.service.FKeepAliveTools;
import com.face_chtj.base_iotutils.KLog;
import com.face_chtj.base_iotutils.ShellUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class ZtKeepAliveService extends Service {
    private static final String TAG = ZtKeepAliveService.class.getSimpleName();
    private Disposable sDisposable;
    private String[] morePkg = new String[]{
            "com.zto.ztoexpresscabinet", "com.ingenious_eyes.cabinet"
            //"com.csdroid.pkg", "jackpal.androidterm"
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        startTask();
    }

    public void startTask() {
        sDisposable = Observable
                .interval(5, 5, TimeUnit.SECONDS)
                //取消任务时取消定时唤醒
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG, "run: close task");
                    }
                })
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        boolean isExeu =FKeepAliveTools.getEableStatus();
                        boolean isExist1 = KSampleTools.isAvilible(morePkg[0]);
                        boolean isExist2 = KSampleTools.isAvilible(morePkg[1]);
                        String addPkg = "";
                        if (isExist1 && isExist2 || isExist1) {
                            addPkg = morePkg[0];
                        } else {
                            if (isExist2) {
                                addPkg = morePkg[1];
                            } else {
                                addPkg = "";
                            }
                        }
                        if (!TextUtils.isEmpty(addPkg)) {
                            KeepAliveData keepAliveData = new KeepAliveData(addPkg, FKeepAliveTools.TYPE_ACTIVITY, isExeu);
                            FKeepAliveTools.addActivity(keepAliveData);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //Log.e(TAG, "accept: ", throwable);
                        closeDisposable();
                        startTask();
                    }
                });
    }


    public void closeDisposable() {
        KLog.d("closeDisposable");
        if (sDisposable != null && !sDisposable.isDisposed()) {
            sDisposable.dispose();
            sDisposable = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        closeDisposable();
    }
}

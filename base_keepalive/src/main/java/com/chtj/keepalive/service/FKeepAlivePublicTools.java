package com.chtj.keepalive.service;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.chtj.keepalive.FileCommonTools;
import com.chtj.keepalive.ShellUtils;
import com.chtj.keepalive.entity.KeepAliveData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class FKeepAlivePublicTools {
    private static final String TAG = FKeepAlivePublicTools.class.getSimpleName();
    public Disposable sDisposable;
    private boolean isKeepAliveStatus = true;
    private static volatile FKeepAlivePublicTools sInstance;
    public static void setKeepAliveStatus(boolean keepAliveStatus) {
        instance().isKeepAliveStatus = keepAliveStatus;
    }

    //单例模式
    private static FKeepAlivePublicTools instance() {
        if (sInstance == null) {
            synchronized (FKeepAlivePublicTools.class) {
                if (sInstance == null) {
                    sInstance = new FKeepAlivePublicTools();
                }
            }
        }
        return sInstance;
    }

    public static void startKeepAlive(Context context) {
        if (instance().sDisposable == null) {
            instance().sDisposable = Observable.interval(5, 11, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
                @Override
                public void accept(Long count) throws Exception {
                    if (!instance().isKeepAliveStatus && instance().sDisposable != null && !instance().sDisposable.isDisposed()) {
                        instance().sDisposable.dispose();
                    }
                    if (FKeepAliveTools.getEableStatus()) {
                        String readJson = FileCommonTools.readFileData();
                        if(!TextUtils.isEmpty(readJson)){
                            List<KeepAliveData> keepAliveDataList = new Gson().fromJson(readJson, new TypeToken<List<KeepAliveData>>() {
                            }.getType());
                            if (keepAliveDataList != null && keepAliveDataList.size() > 0) {
                                for (int i = 0; i < keepAliveDataList.size(); i++) {
                                    if (keepAliveDataList.get(i).getIsEnable()) {
                                        if (keepAliveDataList.get(i).getType() == FKeepAliveTools.TYPE_ACTIVITY) {
                                            FileCommonTools.openApk(context, keepAliveDataList.get(i).getPackageName());
                                        } else if (keepAliveDataList.get(i).getType() == FKeepAliveTools.TYPE_SERVICE) {
                                            if (keepAliveDataList.get(i).getServiceName() != null && !keepAliveDataList.get(i).getServiceName().equals("")) {
                                                FileCommonTools.openService(context, keepAliveDataList.get(i).getPackageName(), keepAliveDataList.get(i).getServiceName());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    Log.d(TAG, "accept: throwable=", throwable);
                    startKeepAlive(context);
                }
            });
        }
    }

    public static void stopKeepAlive() {
        if (instance().sDisposable != null && !instance().sDisposable.isDisposed()) {
            instance().sDisposable.dispose();
        }
    }
}

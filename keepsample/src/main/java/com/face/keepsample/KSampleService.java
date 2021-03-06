package com.face.keepsample;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.chtj.keepalive.FKeepAliveTools;
import com.chtj.keepalive.entity.KeepAliveData;

import java.util.List;

public class KSampleService extends Service {
    private static final String TAG = KSampleService.class.getSimpleName();

    Handler handler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler.postDelayed(runnable, 8000);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //添加一些默认的数据
            List<KeepAliveData> keepAliveDataList = KSampleTools.getDefaultInitData();
            for (int i = 0; i < keepAliveDataList.size(); i++) {
                if (keepAliveDataList.get(i).getType() == FKeepAliveTools.TYPE_ACTIVITY) {
                    boolean isAdded=FKeepAliveTools.isAddedAty(keepAliveDataList.get(i).getPackageName());
                    Log.d(TAG, "run: isAdded="+isAdded);
                    if(!isAdded){
                        //判断是否已经重复添加了 添加了就跳过这一步
                        FKeepAliveTools.addActivity(keepAliveDataList.get(i));
                    }
                } else if (keepAliveDataList.get(i).getType() == FKeepAliveTools.TYPE_SERVICE) {
                    FKeepAliveTools.addService(keepAliveDataList.get(i));
                }
            }
            SPUtils.putBoolean(KSampleService.this, "isFirstInit", false);
            stopService(new Intent(KSampleService.this, KSampleService.class));
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}



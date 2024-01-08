package com.face.keepsample;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.chtj.keepalive.entity.KeepAliveData;
import com.chtj.keepalive.service.FKeepAliveTools;

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
        boolean isFirstInit=SPUtils.getBoolean(this,FKeepAliveTools.FLAG_ISFIRSTINIT,true);
        if(isFirstInit){
            handler.postDelayed(runnable, 5000);
        }
    }

    final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //添加一些默认的数据
            List<KeepAliveData> keepAliveDataList = KSampleTools.getDefaultInitData();
            Log.d(TAG, "run: keepAliveDataList.size >> "+keepAliveDataList.size());
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
            SPUtils.putBoolean(KSampleService.this, FKeepAliveTools.FLAG_ISFIRSTINIT, false);
            stopService(new Intent(KSampleService.this, KSampleService.class));
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}



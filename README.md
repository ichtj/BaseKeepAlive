## Android 项目根目录下文件 build.gradle 中添加

```groovy
allprojects {
	repositories {
		...
		maven { url 'https://www.jitpack.io' }
	}
}
```

## 在App的build.gradle文件中添加

### base_keepalive 服务保活处理 [![](https://jitpack.io/v/wave-chtj/BaseKeepAlive.svg)](https://jitpack.io/#wave-chtj/BaseKeepAlive)

```groovy
dependencies {
         //android 保活
	     implementation 'com.github.wave-chtj:BaseKeepAlive:1.0.1'
}
```



### 自定义 Application

```java
//每个Module library功能描述可在页面下方查看
//别忘了在 Manifest 中通过使用这个自定义的 Application,这里有各个library的初始化
public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
    //④ base_keepalive 服务保活处理
    FBaseDaemon.init(base);
  }
}

```

## ④ base_keepalive 服务保活处理
![image](/pic/keepalive.png)

| 编号 | 模块     | 功能              |
| ---- | -------- | ----------------- |
| 1    | ACTIVITY | activity 界面拉起 |
| 2    | SERVICE  | service 服务拉起  |

#### FKeepAliveTools 直接添加 SERVICE/ACTIVITY

使用方式 直接引用 library

```java
    //添加ACTIVITY拉起
    KeepAliveData keepAliveData = new KeepAliveData("com.xxx.xxx", FKeepAliveTools.TYPE_ACTIVITY, true);
    CommonValue commonValue = FKeepAliveTools.addActivity(keepAliveData);
    if (commonValue == CommonValue.EXEU_COMPLETE) {
        return true;
    } else {
        return false;
    }
    //
    KeepAliveData keepAliveData2 = new KeepAliveData("com.xxx.xxx", FKeepAliveTools.TYPE_SERVICE, "Service的具体路劲com.xxx.xxx.xxService", true);
    CommonValue commonValue = FKeepAliveTools.addService(keepAliveData2);
    if (commonValue == CommonValue.EXEU_COMPLETE) {
        return true;
    } else {
        return false;
    }
```

使用方式 作为一个中间组件调用 其他 app 可以跨进程添加或者清理

```java
    //将base_keepalive中的aidl中的文件复制到自己的项目中
    //将base_keepalive/src/com/chtj/keepalive/entity中的实体类一同复制到自己项目中，并保持包名一致
    //此时这个项目已经作为一个中间件，用于处理所有APP对其的添加，删除操作
    //为什么这样做？有时并不想每个项目都引用这个base_keepalive进行保活，而是在已经存在的library中去直接调用，避免重复造轮子

    public class KeepLiveAty extends BaseActivity implements OnClickListener, IKeepAliveListener {
        private static final String TAG = "KeepLiveAty";
        TextView tvResult;
        /**
         * 保活的类型为Activity
         */
        public static final int TYPE_ACTIVITY = 0;
        /**
         * 保活的类型为服务
         */
        public static final int TYPE_SERVICE = 1;

        IKeepAliveService iKeepAliveService;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            ....
            Intent intent = new Intent();
            intent.setAction("com.chtj.keepalive.IKeepAliveService");
            intent.setPackage("com.face.keepsample");
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
            getData();
        }

        public void getData() {
            try {
                List<KeepAliveData> keepAliveDataList = iKeepAliveService.getKeepLiveInfo();
                if (keepAliveDataList != null && keepAliveDataList.size() > 0) {
                    ToastUtils.success("获取成功 数量=" + keepAliveDataList.size());
                } else {
                    ToastUtils.error("获取失败 数量=0");
                }
            } catch (Exception e) {
                e.printStackTrace();
                KLog.e(TAG, "errMeg:" + e.getMessage());
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_add_aty:
                    try {
                        KeepAliveData keepAliveData = new KeepAliveData("com.wave_chtj.example", TYPE_ACTIVITY, true);
                        boolean isAddAty = iKeepAliveService.addKeepLiveInfo(keepAliveData, this);
                        KLog.d(TAG, "onClick:>btn_add_aty=" + isAddAty);
                        if (isAddAty) {
                            ToastUtils.success("执行成功！");
                        } else {
                            ToastUtils.error("执行失败！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        KLog.e(TAG, "errMeg:" + e.getMessage());
                    }
                    break;
                case R.id.btn_add_service:
                    try {
                        KeepAliveData keepAliveInfo = new KeepAliveData("com.face.baseiotcloud", TYPE_SERVICE, "com.face.baseiotcloud.service.OtherService", true);
                        boolean isaddInfo = iKeepAliveService.addKeepLiveInfo(keepAliveInfo, this);
                        KLog.d(TAG, "onClick:>btn_add_service=" + isaddInfo);
                        if (isaddInfo) {
                            ToastUtils.success("执行成功！");
                        } else {
                            ToastUtils.error("执行失败！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        KLog.e(TAG, "errMeg:" + e.getMessage());
                    }
                    break;
                case R.id.btn_getall:
                    getData();
                    break;
                case R.id.btn_cleanall:
                    try {
                        boolean isClearAll = iKeepAliveService.clearAllKeepAliveInfo();
                        if (isClearAll) {
                            ToastUtils.success("清除成功！");
                        } else {
                            ToastUtils.error("清除失败！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        KLog.e(TAG, "errMeg:" + e.getMessage());
                    }
                    break;
            }
        }

        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                iKeepAliveService = IKeepAliveService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                iKeepAliveService = null;
            }
        };


        IKeepAliveListener.Stub iKeepAliveListener = new IKeepAliveListener.Stub() {
            @Override
            public void onError(String errMeg) throws RemoteException {
                Log.d(TAG, "onError: " + errMeg);
            }

            @Override
            public void onSuccess() throws RemoteException {
                Log.d(TAG, "onSuccess: ");
            }
        };

        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (iKeepAliveService != null) {
                unbindService(conn);
            }
        }

        @Override
        public void onError(String errMeg) throws RemoteException {
            Log.d(TAG, "onError: ");
        }

        @Override
        public void onSuccess() throws RemoteException {
            Log.d(TAG, "onSuccess: ");
        }

        @Override
        public IBinder asBinder() {
            return iKeepAliveListener;
        }
    }
```
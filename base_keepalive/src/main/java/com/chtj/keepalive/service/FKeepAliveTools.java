package com.chtj.keepalive.service;

import android.text.TextUtils;
import android.util.Log;

import com.chtj.keepalive.FileCommonTools;
import com.chtj.keepalive.ShellUtils;
import com.chtj.keepalive.entity.CommonValue;
import com.chtj.keepalive.entity.KeepAliveData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 应用保活
 * 1.用于本地调用添加Activity或者Service保活
 * 2.用于跨进程请使用aidl服务进行添加Activity或者Service保活
 * ----将src/main/aidl目录复制到自己的项目中，然后初始化后调用
 */
public class FKeepAliveTools {
    private static final String TAG = FKeepAliveTools.class.getSimpleName();
    /**
     * 总开关状态
     */
    public static final String PROP_ALLENABLE_KEY = "persist.sys.alive_enable";
    /**
     * 保活的类型为Activity
     */
    public static final int TYPE_ACTIVITY = 0;
    /**
     * 保活的类型为服务
     */
    public static final int TYPE_SERVICE = 1;

    public static final String FLAG_ISFIRSTINIT = "isFirstInit";

    /**
     * 一键总开关状态
     */
    public static boolean getEableStatus() {
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand("getprop " + PROP_ALLENABLE_KEY + "  ", true);
        return commandResult.result == 0 && !TextUtils.isEmpty(commandResult.successMsg) && commandResult.successMsg.contains("true");
    }

    /**
     * 一键总开关状态设置
     */
    public static boolean setEableStatus(boolean status) {
        ShellUtils.CommandResult commandResult = ShellUtils.execCommand("setprop " + PROP_ALLENABLE_KEY +" "+status, true);
        return commandResult.result == 0;
    }

    /**
     * 获取配置文件中的Activity保活数据
     */
    public static KeepAliveData getActData() {
        List<KeepAliveData> keepAliveDataList = FKeepAliveTools.getKeepLive();
        for (int i = 0; i < keepAliveDataList.size(); i++) {
            if (keepAliveDataList.get(i).getType() == FKeepAliveTools.TYPE_ACTIVITY) {
                return keepAliveDataList.get(i);
            }
        }
        return null;
    }

    public static void addActivity(String pkg) {
        FKeepAliveTools.addActivity(new KeepAliveData(pkg, FKeepAliveTools.TYPE_ACTIVITY, true));
    }


    public static void addService(String pkg, String serviceName) {
        FKeepAliveTools.addService(new KeepAliveData(pkg, FKeepAliveTools.TYPE_SERVICE, serviceName, true));
    }

    /**
     * 添加多个保活对象
     *
     * @param keepAliveDataList
     * @return 执行结果
     */
    public static CommonValue addMoreData(List<KeepAliveData> keepAliveDataList) {
        Log.d(TAG, "addMoreData: keepAliveDataList>>" + keepAliveDataList.toString());
        Gson gson = new Gson();
        FileCommonTools.isNeedCreateFile();
        boolean isWrite = FileCommonTools.writeFileData(gson.toJson(keepAliveDataList), true);
        if (!isWrite) {
            return CommonValue.KL_FILE_WRITE_ERR;
        } else {
            //如果是立即启用 那么需要判断服务是否开启
            return CommonValue.EXEU_COMPLETE;
        }
    }

    /**
     * 添加需要拉起的界面 界面只能拉起一个 多个界面会造成混乱
     * 所以调用多次这个方法会覆盖之前那个需要拉起的界面
     *
     * @param keepAliveData 当前需要添加的记录
     * @return 是否执行成功
     */
    public static CommonValue addActivity(KeepAliveData keepAliveData) {
        Log.d(TAG, "addActivity: keepAliveData>>" + keepAliveData.toString());
        Gson gson = new Gson();
        List<KeepAliveData> keepAliveDataList = new ArrayList<>();
        keepAliveData.setServiceName("");
        try {
            String readJson = FileCommonTools.readFileData();
            Log.d(TAG, "addActivity: readJson>>" + readJson);
            keepAliveDataList = gson.fromJson(readJson, new TypeToken<List<KeepAliveData>>() {
            }.getType());
        } catch (Throwable throwable) {
            Log.e(TAG, "addActivity: ", throwable);
        }
        if (keepAliveDataList != null && keepAliveDataList.size() > 0) {
            Iterator<KeepAliveData> it = keepAliveDataList.iterator();
            while (it.hasNext()) {
                KeepAliveData keepData = it.next();
                if (keepData.getType() == TYPE_ACTIVITY) {
                    if (keepAliveData.getPackageName().equals(keepData.getPackageName()) && keepAliveData.getIsEnable() == keepData.getIsEnable()) {
                        /*这里说明添加重复的记录*/
                        Log.d(TAG, "addActivity: exist same pkg info");
                        return CommonValue.EXEU_COMPLETE;
                    }
                    //如果记录中存在Activity的记录 则清除
                    it.remove();
                }
            }
        }
        return toWrite(keepAliveData, keepAliveDataList, gson);
    }

    /**
     * 判断需要保活的应用界面是否存在
     *
     * @param packageName 需要去判断的包名
     * @return 是否已经添加过
     */
    public static boolean isAddedAty(String packageName) {
        Gson gson = new Gson();
        String readJson = FileCommonTools.readFileData();
        List<KeepAliveData> keepAliveDataList = gson.fromJson(readJson, new TypeToken<List<KeepAliveData>>() {
        }.getType());
        if (keepAliveDataList != null && keepAliveDataList.size() > 0) {
            boolean isExist = false;
            for (KeepAliveData kData : keepAliveDataList) {
                if (kData.getType() == TYPE_ACTIVITY && kData.getPackageName().equals(packageName)) {
                    //出现这种情况,应判断为重复添加 告知重复即可 避免因为重复添加导致无法配合界面应用的启用/禁用保活按钮
                    isExist = true;
                    break;
                }
            }
            return isExist;
        }
        return false;
    }


    /**
     * 添加需要保活的服务
     * [包名,服务]一起校验  下一次添加时不可与之前记录中的[包名,服务]一致
     *
     * @param keepAliveData 当前需要添加的记录
     * @return 是否执行成功
     */
    public static CommonValue addService(KeepAliveData keepAliveData) {
        Log.d(TAG, "addService: keepAliveData>>" + keepAliveData.toString());
        List<KeepAliveData> keepAliveDataList = new ArrayList<>();
        Gson gson = new Gson();
        try {
            String readJson = FileCommonTools.readFileData();
            keepAliveDataList = gson.fromJson(readJson, new TypeToken<List<KeepAliveData>>() {
            }.getType());
        } catch (Throwable throwable) {
            Log.e(TAG, "addService: ", throwable);
        }
        if (keepAliveDataList != null && keepAliveDataList.size() > 0) {
            boolean isFind = false;
            for (int i = 0; i < keepAliveDataList.size(); i++) {
                if (keepAliveDataList.get(i).getServiceName() != null && !keepAliveDataList.get(i).getServiceName().equals("")) {
                    if (keepAliveDataList.get(i).getType() == TYPE_SERVICE && keepAliveDataList.get(i).getServiceName().contains(keepAliveData.getServiceName())) {
                        isFind = true;
                        break;
                    }
                }
            }
            if (!isFind) {
                return toWrite(keepAliveData, keepAliveDataList, gson);
            } else {
                return CommonValue.KL_SERVICE_REPEAT;
            }
        } else {
            return toWrite(keepAliveData, null, gson);
        }
    }


    /**
     * 添加拉起的界面,服务
     *
     * @param keepAliveData     当前需要添加的记录
     * @param keepAliveDataList 以前添加的记录
     */
    private static CommonValue toWrite(KeepAliveData keepAliveData, List<KeepAliveData> keepAliveDataList, Gson gson) {
        if (keepAliveDataList == null) {
            keepAliveDataList = new ArrayList<>();
        }
        Log.d(TAG, "toWrite: keepAliveData>>" + keepAliveData.toString() + ",keepAliveDataList>>" + keepAliveDataList.toString());
        keepAliveDataList.add(keepAliveData);
        FileCommonTools.isNeedCreateFile();
        boolean isWrite = FileCommonTools.writeFileData(gson.toJson(keepAliveDataList), true);
        if (!isWrite) {
            return CommonValue.KL_FILE_WRITE_ERR;
        } else {
            //如果是立即启用 那么需要判断服务是否开启
            return CommonValue.EXEU_COMPLETE;
        }
    }

    /**
     * 获取全部的记录Activity+Service
     *
     * @return
     */
    public static List<KeepAliveData> getKeepLive() {
        try {
            Gson gson = new Gson();
            String readJson = FileCommonTools.readFileData();
            List<KeepAliveData> keepAliveDataList = gson.fromJson(readJson, new TypeToken<List<KeepAliveData>>() {
            }.getType());
            return keepAliveDataList;
        } catch (Throwable throwable) {
            return null;
        }
    }

    /**
     * 关闭保活服务
     */
    public static void stopKeepLive() {
        FKeepAlivePublicTools.setKeepAliveStatus(false);
    }
}

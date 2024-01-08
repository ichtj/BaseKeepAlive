package com.chtj.keepalive;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;

import com.chtj.keepalive.entity.CommonValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

public class FileCommonTools {
    private static final String TAG=FileCommonTools.class.getSimpleName();
    public static final String SAVE_KEEPLIVE_PATH = "/sdcard/keepalive/";
    public static final String SAVE_KEEPLIVE_FILE_NAME = "keepalive.txt";

    /**
     * 清除所有的记录
     */
    public static CommonValue clearKeepLive() {
        Log.d(TAG, "clearKeepLive: ");
        File file = new File(SAVE_KEEPLIVE_PATH + SAVE_KEEPLIVE_FILE_NAME);
        if (file.exists()) {
            return file.delete() ? CommonValue.EXEU_COMPLETE : CommonValue.KL_FILE_DEL_ERR;
        } else {
            return CommonValue.KL_DATA_ISNULL;
        }
    }

    public static void isNeedCreateFile() {
        Log.d(TAG, "isNeedCreateFile: ");
        File file = new File(SAVE_KEEPLIVE_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(SAVE_KEEPLIVE_PATH + SAVE_KEEPLIVE_FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
            }
        }
    }



    public static boolean delete() {
        Log.d(TAG, "delete: ");
        File file = new File(SAVE_KEEPLIVE_PATH + SAVE_KEEPLIVE_FILE_NAME);
        if (file.exists()) {
            return file.delete();
        } else {
            //文件不存在时返回false
            return false;
        }
    }


    /**
     * 读取文件内容
     */
    public static String readFileData() {
        String fileName = SAVE_KEEPLIVE_PATH + SAVE_KEEPLIVE_FILE_NAME;
        String result = "";
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                return "";
            }
            FileInputStream fis = new FileInputStream(file);
            //获取文件长度
            int lenght = fis.available();
            byte[] buffer = new byte[lenght];
            fis.read(buffer);
            if (fis != null) {
                fis.close();
            }
            //将byte数组转换成指定格式的字符串
            result = new String(buffer, "UTF-8");
        } catch (Exception e) {
        }
        Log.d(TAG, "readFileData: result>>" + result);
        return result;
    }

    /**
     * 写入数据
     *
     * @param content  写入的内容
     * @param isCover  是否覆盖文件的内容 true 覆盖原文件内容  | flase 追加内容在最后
     * @return 是否成功 true|false
     */
    public static boolean writeFileData(String content, boolean isCover) {
        Log.d(TAG, "writeFileData: ");
        String filename=SAVE_KEEPLIVE_PATH + SAVE_KEEPLIVE_FILE_NAME;
        FileOutputStream fos = null;
        try {
            File file = new File(filename);
            //如果文件不存在
            if (!file.exists()) {
                //重新创建文件
                file.createNewFile();
            }
            fos = new FileOutputStream(file, !isCover);
            byte[] bytes = content.getBytes();
            fos.write(bytes);//将byte数组写入文件
            fos.flush();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "writeFileData: ", e);
        } finally {
            try {
                fos.close();//关闭文件输出流
            } catch (Exception e) {
            }
        }
        return false;
    }

    /**
     * 获取该包名中的主界面
     *
     * @param packageName
     * @return
     */
    private static Intent getAppOpenIntentByPackageName(Context context, String packageName) {
        String mainAct = null;
        PackageManager pkgMag = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("WrongConstant") List<ResolveInfo> list = pkgMag.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < list.size(); i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo.packageName.equals(packageName)) {
                mainAct = info.activityInfo.name;
                break;
            }
        }
        if (TextUtils.isEmpty(mainAct)) {
            return null;
        }
        intent.setComponent(new ComponentName(packageName, mainAct));
        return intent;
    }

    /**
     * 启动第三方apk
     * <p>
     * 如果已经启动apk，则直接将apk从后台调到前台运行（类似home键之后再点击apk图标启动），如果未启动apk，则重新启动
     */
    public static void openApk(Context context, String packName) {
        if (isTopApp(context, packName) && checkAppExistLocal(context, packName)) {
            Intent intent = getAppOpenIntentByPackageName(context, packName);
            context.startActivity(intent);
            Log.d(TAG, "launch this packagename=" + packName);
        } else {
            //Log.d(TAG, " not find this packageName or Already opened this app =" + packName);
        }
    }

    /**
     * 应用是否存在设备中
     *
     * @param context
     * @param packName
     * @return
     */
    public static boolean checkAppExistLocal(Context context, String packName) {
        boolean isFindPkg = false;
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (pinfo.get(i).packageName.equalsIgnoreCase(packName)) {
                isFindPkg = true;
                break;
            }
        }
        return isFindPkg;
    }

    /**
     * 启用其他应用中的Service
     *
     * @param packName           包名
     * @param servicePackageName service包名路径
     */
    public static void openService(Context context, String packName, String servicePackageName) {
        try {
            //Log.d(TAG, "launch this service...  servicePackageName=" + servicePackageName);
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packName, servicePackageName));
            context.startService(intent);
        } catch (Exception e) {
            Log.e(TAG, "errMeg:" + e.getMessage());
        }
    }

    /**
     * 判断该包名的应用是否存在
     *
     * @param packageName
     * @return
     */
    private static boolean isTopApp(Context context, String packageName) {
        boolean isNeedStartAty = false;
        ActivityManager am = (ActivityManager) context.getSystemService("activity");
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            String topPkg = ((ActivityManager.RunningTaskInfo) list.get(0)).topActivity.getPackageName();
            //return list != null ? ((RunningTaskInfo)list.get(0)).topActivity.getPackageName() : null;
            if (topPkg != null && packageName.equals(topPkg)) {
                //证明应用已存在 并且已运行在前台
                isNeedStartAty = false;
            } else {
                //表示apk未运行在前台 或者包名不存在
                isNeedStartAty = true;
            }
        }
        return isNeedStartAty;
    }
}

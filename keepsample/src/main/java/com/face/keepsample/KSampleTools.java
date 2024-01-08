package com.face.keepsample;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.chtj.keepalive.entity.KeepAliveData;
import com.chtj.keepalive.service.FKeepAliveTools;
import com.face_chtj.base_iotutils.BaseIotUtils;

import java.util.ArrayList;
import java.util.List;

public class KSampleTools {
    /**
     * 应用首次启动默认添加一些数据
     * @return
     */
    public static List<KeepAliveData> getDefaultInitData() {
        List<KeepAliveData> keepAliveDataList = new ArrayList<>();
        keepAliveDataList.add(new KeepAliveData("com.face.baseiotcloud", FKeepAliveTools.TYPE_SERVICE,"com.face.baselib.service.OtherService",true));
        return keepAliveDataList;
    }

    /**
     * 通过包名获取应用程序的名称。
     *
     * @param packageName
     *            包名。
     * @return 返回包名所对应的应用程序的名称。
     */
    public static String getAppNameByPkg(String packageName) {
        try {
            PackageManager pm = BaseIotUtils.getContext().getPackageManager();
            return pm.getApplicationLabel(
                    pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 通过包名获取应用程序的名称。
     *
     * @param packageName
     *            包名。
     * @return 返回包名所对应的应用程序的名称。
     */
    public static Drawable getAppIconByPkg(String packageName) {
        try {
            ApplicationInfo aInfo =BaseIotUtils.getContext(). getPackageManager().getApplicationInfo(packageName,PackageManager.GET_META_DATA);
            return BaseIotUtils.getContext().getPackageManager().getApplicationIcon(aInfo);
        }catch (Exception e){
            return ContextCompat.getDrawable(BaseIotUtils.getContext(),R.mipmap.load_err);
        }
    }

    public static boolean isAvilible(String packageName){
        final PackageManager packageManager = BaseIotUtils.getContext().getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            // 循环判断是否存在指定包名
            if (pinfo.get(i).packageName.equalsIgnoreCase(packageName)) {
                return true;
            }
        }
        return false;
    }
}

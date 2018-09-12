package com.example.naruto.guidetosettingboot;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MobileInfoUtils {
    private static final String TAG = "MobileInfoUtils";

    /**
     * Get Mobile Type
     *
     * @return
     */
    private static String getMobileType() {
        return Build.MANUFACTURER;
    }

    /**
     * @return
     */
    public static ComponentName getBootSettingComponentName() {
        ComponentName componentName = null;
        String mobileType = getMobileType();
        String activityString = "";
        switch (mobileType) {
            case "OPPO":
                activityString = "com.oppo.safe/.permission.startup.StartupAppListActivity";// ColorOS V2.1 实测可行
                break;
            case "Xiaomi":
                activityString = "com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity";//MIUI 9.2 实测可行
                break;
            case "HUAWEI":
//                    activityString="com.huawei.systemmanager/com.huawei.systemmanager.optimize.process.ProtectActivity";
//                    activityString="com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity";
                activityString = "com.huawei.systemmanager/com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity";//EMUI 8.0.0 实测可行
                break;
            case "Meizu":
//                    activityString = "com.meizu.safe/.permission.PermissionMainActivity";
                activityString = "com.meizu.safe/com.meizu.safe.security.HomeActivity";//Flyme OS 4.5.7A 实测可行
                break;
            case "samsung":
                activityString = "com.samsung.android.sm_cn/com.samsung.android.sm.ui.ram.AutoRunActivity";// 三星Note5测试通过
                break;
            case "vivo":
//                activityString = "com.iqoo.secure/.safeguard.PurviewTabActivity";
                activityString = "com.vivo.permissionmanager/com.vivo.permissionmanager.activity.BgStartUpManagerActivity";
                break;
            case "ulong": // 360手机 未测试
                activityString = "com.yulong.android.coolsafe/.ui.activity.autorun.AutoRunListActivity";
                break;
            default:
                //不是市面主流品牌的手机不用管（已经设置了启动广播，如果是原生系统就不需要用户手动设置，如果非原生系统，反正不是主流品牌，直接放弃）
                break;
        }

        if (!TextUtils.isEmpty(activityString)) {
            componentName = ComponentName.unflattenFromString(activityString);
        }
        return componentName;
    }

    /**
     * 跳转到设置自启动页面
     *
     * @param context
     * @param componentName
     */
    public static void jumpToBootSettingActivity(final Context context, ComponentName componentName) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(componentName);
        boolean isOccurredException = false;//是否发生异常
        try {
            context.startActivity(intent);
        } catch (Exception e) {//抛出异常就直接打开设置页面
            e.printStackTrace();
            isOccurredException = true;
            Log.e(TAG, "jumpStartInterface: 跳转失败");
            intent = new Intent(Settings.ACTION_SETTINGS);
            context.startActivity(intent);
        }
        if (!isOccurredException) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    openGuideFloatingWindow(context);
                }
            }, 300);
        }
    }

    /**
     * 打开引导设置悬浮窗
     *
     * @param context
     */
    private static void openGuideFloatingWindow(Context context) {
        final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0及以上
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {//7.0以上
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_TOAST;//这个无需悬浮窗权限
        }

        mParams.format = PixelFormat.TRANSLUCENT;// 支持透明
        //mParams.format = PixelFormat.RGBA_8888;
        //mParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;// 焦点
        mParams.width = WindowManager.LayoutParams.MATCH_PARENT;//窗口的宽和高
        mParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mParams.x = 0;//窗口位置的偏移量
        mParams.y = 0;

        //mParams.alpha = 0.5f;//窗口的透明度
        final View contentView = LayoutInflater.from(context).inflate(R.layout.floating_window_guide, null);
        final View button = contentView.findViewById(R.id.bt_go_setting);
        contentView.setFocusableInTouchMode(true);
        String mobileType = getMobileType().toLowerCase();
        if (mobileType.equals("huawei")) {
            contentView.findViewById(R.id.iv_slide_up).setVisibility(View.GONE);
        } else {
            contentView.findViewById(R.id.tv_step_two).setVisibility(View.GONE);
            contentView.findViewById(R.id.iv_step_two).setVisibility(View.GONE);
            TextView tv = (TextView) contentView.findViewById(R.id.tv_step_one);
            tv.setText("滑动列表找到“" + context.getString(R.string.app_name) + "”");
            FilletedCornerStrokeImageView iv = (FilletedCornerStrokeImageView) contentView.findViewById(R.id.iv_step_one);
            switch (mobileType) {
                case "xiaomi":
                    iv.setImageResource(R.drawable.boot_setting_switch_xiaomi);
                    break;
                case "meizu":
                    iv.setImageResource(R.drawable.boot_setting_switch_meizu);
                    break;
            }
        }
        contentView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                boolean result = false;
                switch (keyCode) {
                    case KeyEvent.KEYCODE_BACK:
                    case KeyEvent.KEYCODE_HOME:
                        button.callOnClick();
                        result = true;
                        break;
                }
                return result;
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wm != null && contentView != null && contentView.getParent() != null)
                    wm.removeView(contentView);//移除窗口
            }
        });
        try {
            wm.addView(contentView, mParams);//添加窗口
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "openGuideFloatingWindow: 添加窗口失败");
        }
    }

    /**
     * 检查悬浮窗权限
     *
     * @param context
     * @return
     */
    public static boolean checkFloatPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                Class cls = Class.forName("android.content.Context");
                Field declaredField = cls.getDeclaredField("APP_OPS_SERVICE");
                declaredField.setAccessible(true);
                Object obj = declaredField.get(cls);
                if (!(obj instanceof String)) {
                    return false;
                }
                String str2 = (String) obj;
                obj = cls.getMethod("getSystemService", String.class).invoke(context, str2);
                cls = Class.forName("android.app.AppOpsManager");
                Field declaredField2 = cls.getDeclaredField("MODE_ALLOWED");
                declaredField2.setAccessible(true);
                Method checkOp = cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String.class);
                int result = (Integer) checkOp.invoke(obj, 24, Binder.getCallingUid(), context.getPackageName());
                return result == declaredField2.getInt(cls);
            } catch (Exception e) {
                return false;
            }
        } else {
            boolean result = true;
            if (Build.VERSION.SDK_INT >= 26) {//8.0以上
                AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsMgr == null) {
                    result = false;
                } else {
                    int mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context
                            .getPackageName());
                    result = mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED;
                }
            }
            return result && Settings.canDrawOverlays(context);
        }
    }

    /**
     * GoTo Open Self Setting Layout
     * Compatible Mainstream Models 兼容市面主流机型
     *
     * @param context
     */
    public static void jumpStartInterface(Context context) {
        Intent intent = new Intent();
        String mobileType = getMobileType();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Log.d(TAG, "jumpStartInterface: 当前手机型号为：" + mobileType);
        String activityString = "";
        switch (mobileType) {
            case "OPPO":
                activityString = "com.oppo.safe/.permission.startup.StartupAppListActivity";// ColorOS V2.1 实测可行
                break;
            case "Xiaomi":
                activityString = "com.miui.securitycenter/com.miui.permcenter.autostart.AutoStartManagementActivity";//MIUI 9.2 实测可行
                break;
            case "HUAWEI":
//                    activityString="com.huawei.systemmanager/com.huawei.systemmanager.optimize.process.ProtectActivity";
//                    activityString="com.huawei.systemmanager/.startupmgr.ui.StartupNormalAppListActivity";
                activityString = "com.huawei.systemmanager/com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity";//EMUI 8.0.0 实测可行
                break;
            case "Meizu":
//                    activityString = "com.meizu.safe/.permission.PermissionMainActivity";
                activityString = "com.meizu.safe/com.meizu.safe.security.HomeActivity";//Flyme OS 4.5.7A 实测可行
                break;
            case "samsung":
                activityString = "com.samsung.android.sm_cn/com.samsung.android.sm.ui.ram.AutoRunActivity";// 三星Note5测试通过
                break;
            case "vivo":
//                activityString = "com.iqoo.secure/.safeguard.PurviewTabActivity";
                activityString = "com.vivo.permissionmanager/com.vivo.permissionmanager.activity.BgStartUpManagerActivity";
                break;
            case "ulong": // 360手机 未测试
                activityString = "com.yulong.android.coolsafe/.ui.activity.autorun.AutoRunListActivity";
                break;
        }
        if (TextUtils.isEmpty(activityString)) {//不是市面主流品牌的手机不用管（已经设置了启动广播，如果是原生系统就不需要用户手动设置，如果非原生系统，反正不是主流品牌，直接放弃）
            intent = null;
/*            // 以上只是市面上主流机型，由于公司你懂的，所以很不容易才凑齐以上设备
            // 针对于其他设备，我们只能调整当前系统app查看详情界面
            // 在此根据用户手机当前版本跳转系统设置界面
            if (Build.VERSION.SDK_INT >= 9) {
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
            } else if (Build.VERSION.SDK_INT <= 8) {
                intent.setAction(Intent.ACTION_VIEW);
                intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
            }*/
        } else {
            ComponentName componentName = ComponentName.unflattenFromString(activityString);
            intent.setComponent(componentName);
        }

        if (intent != null) {
            try {
                context.startActivity(intent);
            } catch (Exception e) {//抛出异常就直接打开设置页面
                e.printStackTrace();
                Log.e(TAG, "jumpStartInterface: 跳转失败");
                intent = new Intent(Settings.ACTION_SETTINGS);
                context.startActivity(intent);
            }
        }
    }

    /**
     * @Purpose 
     * @Author Naruto Yang
     * @CreateDate 2018/9/12 0012
     * @Note
     */
    public static class BootSettingHelper {
        private ComponentName componentName;
        private Activity activity;

        public BootSettingHelper(Activity activity) {
            this.activity = activity;
        }

        /**
         * 引导用户设置开机自启
         */
        public void guideToBootSetting(boolean isNeedDialog, final int requestCode) {
            componentName = MobileInfoUtils.getBootSettingComponentName();
            if (componentName != null) {
                if (isNeedDialog) {
                    DialogUtils.showDialog(activity, true, activity.getString(R.string.app_name) + "提示", "为保证正常收到消息通知，需要开启重要权限", true, "开启", "取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            guideToBootSetting(requestCode);
                        }
                    }, null);
                } else {
                    guideToBootSetting(requestCode);
                }
            }
        }

        private void guideToBootSetting(final int requestCode) {
            if (MobileInfoUtils.checkFloatPermission(activity)) {
                MobileInfoUtils.jumpToBootSettingActivity(activity, componentName);
            } else {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    DialogUtils.showDialog(activity, true, "开启悬浮窗权限", "请在下一个页面找到“显示悬浮窗”或“在其他应用上层显示”选项开关,并开启", true, "确定", null, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activity.startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName())), requestCode);
                        }
                    }, null);
                } else {//7.0及以下无需悬浮窗权限
                    MobileInfoUtils.jumpToBootSettingActivity(activity, componentName);
                }
            }
            //MobileInfoUtils.jumpToBootSettingActivity(MainActivity.this, componentName);
        }

        /**
         * 申请悬浮窗权限页面返回
         */
        public void afterRequestFloatWindowPeermission() {
            if (MobileInfoUtils.checkFloatPermission(activity)) {
                MobileInfoUtils.jumpToBootSettingActivity(activity, componentName);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (MobileInfoUtils.checkFloatPermission(activity)) {
                            MobileInfoUtils.jumpToBootSettingActivity(activity, componentName);
                        } else {
                            Toast.makeText(activity, "悬浮窗权限未开启", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, 500);
            }
        }
    }

}
package com.example.naruto.guidetosettingboot;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

/**
 * @Purpose
 * @Author Naruto Yang
 * @CreateDate 2018/9/12 0012
 * @Note
 */
public class BootSettingHelper {
    private ComponentName componentName;
    private Activity activity;
    private View guideWindowView;
    private HomeListener homeListener;
    private static final String TAG = "BootSettingHelper";

    public BootSettingHelper(Activity activity) {
        this.activity = activity;
    }

    public void initComponentName() {
        componentName = getBootSettingComponentName();
    }

    private void initHomeListener() {
        if (homeListener != null) {
            return;
        }
        homeListener = new HomeListener(activity);
        homeListener.setInterface(new HomeListener.Action() {
            @Override
            public void home() {
                hideGuideWindow();
            }

            @Override
            public void recent() {
                hideGuideWindow();
            }

            @Override
            public void longHome() {
                hideGuideWindow();
            }
        });
    }


    /**
     * 引导用户设置开机自启
     *
     * @param isNeedDialog 在引导前是否需要弹窗提示用户前往（自动调用时需要，若是用户手动点击“权限设置”则不需要）
     * @param requestCode  申请悬浮窗权限的requestCode
     */
    public void guideToBootSetting(boolean isNeedDialog, final int requestCode) {
        if (componentName == null) {
            initComponentName();
        }
        if (componentName != null) {
            if (isNeedDialog) {
                DialogUtils.showDialog(activity, true, activity.getString(R.string.app_name) + "提示", "为保证正常收到消息通知，需要开启重要权限", false, "开启", "取消",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                guideToBootSetting(requestCode);
                            }
                        }, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String message = "您已取消本次权限设置，后续可前往“我”>“个人信息”>“权限设置”重新设置";
                                DialogUtils.showDialog(activity, true, null, message, true, "确定", null, null, null);
                            }
                        });
            } else {
                guideToBootSetting(requestCode);
            }
        }
    }


    /**
     * 引导用户设置开机自启
     *
     * @param requestCode 申请悬浮窗权限的requestCode
     */
    private void guideToBootSetting(final int requestCode) {
        if (MobileInfoUtils.checkFloatPermission(activity)) {
            jumpToBootSettingActivity(activity, componentName, this);
        } else {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                DialogUtils.showDialog(activity, true, "开启悬浮窗权限", "请在下一个页面找到“显示悬浮窗”或“在其他应用上层显示”选项开关,并开启", false, "确定", null, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName())), requestCode);
                    }
                }, null);
            } else {//7.0及以下无需悬浮窗权限
                jumpToBootSettingActivity(activity, componentName, this);
            }
        }
        //MobileInfoUtils.jumpToBootSettingActivity(MainActivity.this, componentName);
    }

    /**
     * 申请悬浮窗权限页面返回
     */
    public void afterRequestFloatWindowPeermission() {
        if (MobileInfoUtils.checkFloatPermission(activity)) {
            jumpToBootSettingActivity(activity, componentName, this);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (MobileInfoUtils.checkFloatPermission(activity)) {
                        jumpToBootSettingActivity(activity, componentName, BootSettingHelper.this);
                    } else {
                        Toast.makeText(activity, "悬浮窗权限未开启", Toast.LENGTH_SHORT).show();
                    }
                }
            }, 500);
        }
    }

    /**
     * 移除引导遮罩层
     */
    private void hideGuideWindow() {
        if (guideWindowView != null) {
            try {
                View button = guideWindowView.findViewById(R.id.bt_go_setting);
                button.callOnClick();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        homeListener.stopListen();
    }

    public ComponentName getComponentName() {
        return componentName;
    }

    public View getGuideWindowView() {
        return guideWindowView;
    }

    public void setGuideWindowView(View guideWindowView) {
        this.guideWindowView = guideWindowView;
        initHomeListener();
        homeListener.startListen();
    }

    public HomeListener getHomeListener() {
        return homeListener;
    }

    /**
     * @return
     */
    public static ComponentName getBootSettingComponentName() {
        ComponentName componentName = null;
        String mobileType = MobileInfoUtils.getMobileType();
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
    public static void jumpToBootSettingActivity(final Context context, ComponentName componentName, final BootSettingHelper bootSettingHelper) {
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
                    View view = openGuideFloatingWindow(context);
                    if (bootSettingHelper != null) {
                        bootSettingHelper.setGuideWindowView(view);
                    }
                }
            }, 300);
        }
    }

    /**
     * 打开引导设置悬浮窗
     *
     * @param context
     */
    private static View openGuideFloatingWindow(Context context) {
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
        String mobileType = MobileInfoUtils.getMobileType().toLowerCase();
        TextView tvStepOne = (TextView) contentView.findViewById(R.id.tv_step_one);
        TextView tvOpen = (TextView) contentView.findViewById(R.id.tv_step_open);
        if (mobileType.equals("huawei")) {
            contentView.findViewById(R.id.iv_slide_up).setVisibility(View.GONE);
            tvOpen.setVisibility(View.GONE);
            tvStepOne.setText("第一步：关闭“" + context.getString(R.string.app_name) + "”自动管理");
        } else {
            contentView.findViewById(R.id.tv_huawei).setVisibility(View.GONE);
            contentView.findViewById(R.id.tv_step_two).setVisibility(View.GONE);
            contentView.findViewById(R.id.iv_step_two).setVisibility(View.GONE);
            tvStepOne.setText("滑动列表找到“" + context.getString(R.string.app_name) + "”");
            tvOpen.setText("开启“" + context.getString(R.string.app_name) + "”的自启动开关");
            ImageView iv = (ImageView) contentView.findViewById(R.id.iv_switch);
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
                if (wm != null && contentView != null && contentView.getParent() != null) {
                    wm.removeView(contentView);//移除窗口
                    EventBus.getDefault().post(new BootSettingEvent());
                }
            }
        });
        try {
            wm.addView(contentView, mParams);//添加窗口
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "openGuideFloatingWindow: 添加窗口失败");
        }
        return contentView;
    }

    /**
     * 此方法已过时，但还有点参考价值
     * GoTo Open Self Setting Layout
     * Compatible Mainstream Models 兼容市面主流机型
     *
     * @param context
     */
    private static void jumpStartInterface(Context context) {
        Intent intent = new Intent();
        String mobileType = MobileInfoUtils.getMobileType();
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

}

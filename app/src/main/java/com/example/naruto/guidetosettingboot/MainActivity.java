package com.example.naruto.guidetosettingboot;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 115;
    private static final String TAG = "MainActivity";
    private ComponentName componentName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            Log.e(TAG, "onActivityResult: resultCode=" + resultCode);
            if (MobileInfoUtils.checkFloatPermission(this)) {
                MobileInfoUtils.jumpToBootSettingActivity(this, componentName);
            } else {
                Toast.makeText(this,"悬浮窗权限未开启",Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }

    public void test(View view) {
        componentName = MobileInfoUtils.getBootSettingComponentName();
        if (componentName != null) {
            DialogUtils.showDialog(this, true, "犀牛云客提示", "为保证正常收到消息通知，需要开启重要权限", true, "开启", "取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (MobileInfoUtils.checkFloatPermission(MainActivity.this)) {
                        MobileInfoUtils.jumpToBootSettingActivity(MainActivity.this, componentName);
                    } else {
                        if (Build.VERSION.SDK_INT >= 23) {
                            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), REQUEST_CODE_OVERLAY_PERMISSION);
                        }
                    }
                    //MobileInfoUtils.jumpToBootSettingActivity(MainActivity.this, componentName);
                }
            }, null);
        }
    }
}

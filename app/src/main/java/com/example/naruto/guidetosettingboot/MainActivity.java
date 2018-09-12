package com.example.naruto.guidetosettingboot;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 115;
    private static final String TAG = "MainActivity";
    private MobileInfoUtils.BootSettingHelper bootSettingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bootSettingHelper = new MobileInfoUtils.BootSettingHelper(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            Log.e(TAG, "onActivityResult: resultCode=" + resultCode);
            bootSettingHelper.afterRequestFloatWindowPeermission();
            return;
        }
    }

    public void test(View view) {
        bootSettingHelper.guideToBootSetting(true, REQUEST_CODE_OVERLAY_PERMISSION);
    }
}

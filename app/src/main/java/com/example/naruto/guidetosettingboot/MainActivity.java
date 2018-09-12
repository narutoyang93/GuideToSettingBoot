package com.example.naruto.guidetosettingboot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 115;
    private static final String TAG = "MainActivity";
    private BootSettingHelper bootSettingHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bootSettingHelper = new BootSettingHelper(this);
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

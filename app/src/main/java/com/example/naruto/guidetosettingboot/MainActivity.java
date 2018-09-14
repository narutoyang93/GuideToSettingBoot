package com.example.naruto.guidetosettingboot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OVERLAY_PERMISSION = 115;
    private static final String TAG = "MainActivity";
    private BootSettingHelper bootSettingHelper;
    private boolean isNeedInitXGPush = false;//是否需要初始化信鸽推送

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        bootSettingHelper = new BootSettingHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isNeedInitXGPush) {
            Log.d(TAG, "onResume: 初始化信鸽推送");
            isNeedInitXGPush = false;
            //初始化信鸽推送
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_OVERLAY_PERMISSION) {
            Log.e(TAG, "onActivityResult: resultCode=" + resultCode);
            bootSettingHelper.afterRequestFloatWindowPeermission();
            return;
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().removeStickyEvent(this);
        EventBus.getDefault().unregister(this);
        try {
            bootSettingHelper.getHomeListener().stopListen();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * 接收eventBus
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(BootSettingEvent event) {
        isNeedInitXGPush = true;
        Log.d(TAG, "onMessageEvent: ");
    }

    public void test(View view) {
        bootSettingHelper.guideToBootSetting(true, REQUEST_CODE_OVERLAY_PERMISSION);
    }
}

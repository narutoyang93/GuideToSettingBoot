package com.example.naruto.guidetosettingboot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * @Purpose
 * @Author Naruto Yang
 * @CreateDate 2018/9/12 0012
 * @Note
 */
public class HomeListener {
    public Action mAction;
    public Context mContext;
    public IntentFilter mHomeBtnIntentFilter = null;
    public HomeBtnReceiver mHomeBtnReceiver = null;
    public static final String TAG = "HomeListener";

    public HomeListener(Context context) {
        mContext = context;
        mHomeBtnIntentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        mHomeBtnReceiver = new HomeBtnReceiver();
    }

    public void startListen() {
        if (mContext != null) {
            mContext.registerReceiver(mHomeBtnReceiver, mHomeBtnIntentFilter);
        }
    }

    public void stopListen() {
        if (mContext != null) {
            mContext.unregisterReceiver(mHomeBtnReceiver);
        }
    }

    public void setInterface(Action action) {
        mAction = action;

    }

    class HomeBtnReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra("reason");
                if (reason != null) {
                    if (null != mAction) {
                        if (reason.equals("homekey")) {
                            //按Home按键
                            mAction.home();
                        } else if (reason.equals("recentapps")) {
                            //最近任务键也就是菜单键
                            mAction.recent();
                        } else if (reason.equals("assist")) {
                            //常按home键盘
                            mAction.longHome();
                        }
                    }
                }
            }
        }
    }

    public interface Action {
        public void home();

        public void recent();

        public void longHome();
    }
}

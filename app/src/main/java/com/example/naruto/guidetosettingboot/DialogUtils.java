package com.example.naruto.guidetosettingboot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * @Purpose
 * @Author Naruto Yang
 * @CreateDate 2018/7/27 0027
 * @Note
 */
public class DialogUtils {
    public static final SparseArray<Dialog> progressDialogSparseArray = new SparseArray<>();
    public static final int KEY_BACK_OPERATION_FINISH = 0;//按下返回键直接finish当前activity
    public static final int KEY_BACK_OPERATION_DISMISS = 1;//按下返回键仅dismiss dialog

    private static final String TAG = "DialogUtils";

    /**
     * 显示消息弹窗
     *
     * @param context
     * @param title
     * @param message
     * @param isCancelable     //点击弹窗区域外是否自动消失
     * @param okButtonText
     * @param cancelButtonText
     * @param OnOkClick
     * @param onCancelClick
     */
    public static Dialog showDialog(Context context, boolean isUseBeautifyLayout, String title, String message, boolean isCancelable,
                                    String okButtonText, String cancelButtonText,
                                    View.OnClickListener OnOkClick, View.OnClickListener onCancelClick) {
        int layoutRes = isUseBeautifyLayout ? R.layout.dialog_general_beautify : R.layout.dialog_general;
        LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(layoutRes, null);
        final Dialog dialog = new AlertDialog.Builder(context).create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0x00ffffff));//使dialog自带背景透明，这样才能看到圆角效果（如果布局背景有设置圆角的话）
        dialog.setCancelable(isCancelable);
        dialog.show();
        dialog.getWindow().setContentView(layout);

        TextView tvTitle = (TextView) layout.findViewById(R.id.tv_title);
        TextView messageTextView = (TextView) layout.findViewById(R.id.message);
        Button okButton = (Button) layout.findViewById(R.id.okButton);
        Button cancelButton = (Button) layout.findViewById(R.id.cancelButton);

        if (TextUtils.isEmpty(title)) {// 没有title
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setText(title);
        }

        messageTextView.setText(message);// 设置弹窗消息内容
        // 没有按钮
        if (TextUtils.isEmpty(cancelButtonText) && TextUtils.isEmpty(okButtonText)) {
            LinearLayout buttonLayout = (LinearLayout) layout.findViewById(R.id.button);
            buttonLayout.setVisibility(View.GONE);
        } else {
            if (TextUtils.isEmpty(cancelButtonText)) {// 没有取消按钮
                if (isUseBeautifyLayout) {
                    layout.findViewById(R.id.v_space).setVisibility(View.GONE);
                } else {
                    layout.findViewById(R.id.line2).setVisibility(View.GONE);
                }
                cancelButton.setVisibility(View.GONE);
            } else {
                cancelButton.setText(cancelButtonText);
                cancelButton.setOnClickListener(new DefaultClickListener(dialog, onCancelClick));
            }
            okButton.setText(okButtonText);
            okButton.setOnClickListener(new DefaultClickListener(dialog, OnOkClick));
        }

        return dialog;
    }


    /**
     * 显示加载动画
     *
     * @param contentView
     * @param activity
     * @param isCancelable
     * @param onKeyListener
     * @param operation
     */
    public static void showProgressDialog(final View contentView, final Activity activity, final boolean isCancelable, final DialogInterface.OnKeyListener onKeyListener, final int operation) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int activityHashCode = activity.hashCode();
                    Log.d(TAG, "run: activityHashCode=" + activityHashCode);
                    Dialog dialog = progressDialogSparseArray.get(activityHashCode);
                    if (dialog == null) {
                        WeakReference<Activity> weakReference = new WeakReference<>(activity);
                        Context context = weakReference.get();
                        if (context != null) {
                            dialog = new AlertDialog.Builder(context, R.style.transDialogTheme).create();
                            dialog.setCancelable(isCancelable);
                            dialog.setOnKeyListener(onKeyListener != null ? onKeyListener : new DialogInterface.OnKeyListener() {
                                @Override
                                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                    boolean b = false;
                                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                                        dismissProgressDialog(activity);
                                        switch (operation) {
                                            case KEY_BACK_OPERATION_FINISH:
                                                activity.finish();
                                                break;
                                        }
                                    }
                                    return b;
                                }
                            });
                            dialog.show();
                            Window window = dialog.getWindow();
                            window.setContentView(contentView == null ? new ProgressBar(context) : contentView);

/*                            //设置大小
                            if (layoutParams != null) {
                                WindowManager.LayoutParams params = window.getAttributes();
                                params.width = layoutParams.width;
                                params.height = layoutParams.height;
                                window.setAttributes(params);
                            }*/

                            progressDialogSparseArray.put(activityHashCode, dialog);
                        }
                    } else if (!dialog.isShowing()) {
                        dialog.show();
                    }
                }
            });
        }
    }

    /**
     * 显示遮罩层（禁止用户点击屏幕）
     *
     * @param activity
     * @return
     */
    public static void showProgressMaskLayer(Activity activity, int operation) {
        if (activity != null) {
            ProgressBar progressBar = new ProgressBar(activity);
            progressBar.setIndeterminate(false);
            progressBar.setIndeterminateDrawable(activity.getResources().getDrawable(R.drawable.loading));
            progressBar.setLayoutParams(new ViewGroup.LayoutParams(dip2px(activity, 30), dip2px(activity, 30)));

            RelativeLayout relativeLayout = new RelativeLayout(activity);
            relativeLayout.setGravity(Gravity.CENTER);
            relativeLayout.addView(progressBar);
            showProgressDialog(relativeLayout, activity, false, null, operation);
        }
    }

    public static void showProgressMaskLayer(Activity activity) {
        showProgressMaskLayer(activity, KEY_BACK_OPERATION_DISMISS);
    }

    /**
     * @param activity
     */
    public static void dismissProgressDialog(Activity activity) {
        final Dialog dialog = progressDialogSparseArray.get(activity.hashCode());
        if (dialog != null && dialog.isShowing() && activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }
            });
        }
    }

    /**
     * 默认点击事件处理接口
     */
    public static class DefaultClickListener implements View.OnClickListener {
        private Dialog dialog;
        private View.OnClickListener onClickListener;

        public DefaultClickListener(Dialog dialog, View.OnClickListener onClickListener) {
            this.dialog = dialog;
            this.onClickListener = onClickListener;
        }

        @Override
        public void onClick(View v) {
            dialog.dismiss();
            if (onClickListener != null) {
                onClickListener.onClick(v);
            }
        }
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context,float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}

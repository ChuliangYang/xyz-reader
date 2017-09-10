package com.example.xyzreader.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.example.xyzreader.R;


public class WidgetHelper {
    public static ProgressDialog createProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context, R.style.CustomDialog);
        ProgressBar mProgressBar = new ProgressBar(context);
        mProgressBar.setBackgroundColor(context.getResources().getColor(R.color.theme_accent));
        FrameLayout rootContainer = new FrameLayout(context);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        mProgressBar.setLayoutParams(lp);
        rootContainer.addView(mProgressBar);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        return progressDialog;
    }
}

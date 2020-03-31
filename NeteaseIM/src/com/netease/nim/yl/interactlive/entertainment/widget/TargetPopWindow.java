package com.netease.nim.yl.interactlive.entertainment.widget;

import android.app.Activity;
import android.view.View;
import com.netease.nim.yl.R;
import com.netease.nim.yl.interactlive.entertainment.activity.LiveActivity;
import com.netease.nim.yl.main.utils.YLPopWindow;

/**
 * Created by shenhuan on 2019/5/21 0021.
 */

public class TargetPopWindow implements View.OnClickListener {
    /**
     * 来源：班级/学校/..
     */
    private Activity mActivity;
    private YLPopWindow mPopWindow;

    private String classId = "";

    public TargetPopWindow(Activity activity) {
        mActivity = activity;

    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public void show(View view) {
        if (mPopWindow == null) {
            createPopWindow();
        }
        mPopWindow.showAsDropDown(view, -10, -(mPopWindow.getHeight() + view.getHeight()));
    }

    private void dismiss() {
        if (mPopWindow != null) {
            mPopWindow.dismiss();
        }
    }

    public void destroy() {
        dismiss();
        mPopWindow = null;
    }

    private void createPopWindow() {
        View view = View.inflate(mActivity, R.layout.pop_menu_target_type, null);
        view.findViewById(R.id.ll_target_all).setOnClickListener(this);
        view.findViewById(R.id.ll_target_teacher).setOnClickListener(this);
        view.findViewById(R.id.ll_target_parents).setOnClickListener(this);
        mPopWindow = new YLPopWindow.PopupWindowBuilder(mActivity).setView(view).create();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ll_target_all) {
            ((LiveActivity)mActivity).setTargetType("a");
        } else if (i == R.id.ll_target_teacher) {
            ((LiveActivity)mActivity).setTargetType("t");
        } else if (i == R.id.ll_target_parents) {
            ((LiveActivity)mActivity).setTargetType("p");
        } else {
            return;
        }
        dismiss();
    }
}


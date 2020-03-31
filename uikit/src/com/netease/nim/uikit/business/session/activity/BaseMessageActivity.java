package com.netease.nim.uikit.business.session.activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.fragment.MessageFragment;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.util.StatusBarUtils;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

import java.util.List;

/**
 * Created by zhoujianghua on 2015/9/10.
 */
public abstract class BaseMessageActivity extends UI {

    protected String sessionId;

    private SessionCustomization customization;

    private MessageFragment messageFragment;

    protected abstract MessageFragment fragment();

    protected abstract int getContentViewId();

    protected TextView mTvTitle;
    private LinearLayout mLlTitleRight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        StatusBarUtils.setColor(this, ContextCompat.getColor(this, R.color.theme_color));
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mLlTitleRight = (LinearLayout) findViewById(R.id.ll_title_right);
        messageFragment = (MessageFragment) switchContent(fragment());
        parseIntent();
    }

    @Override
    public void onBackPressed() {
        if (messageFragment == null || !messageFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (messageFragment != null) {
            messageFragment.onActivityResult(requestCode, resultCode, data);
        }

        /*if (customization != null) {
            customization.onActivityResult(this, requestCode, resultCode, data);
        }*/
    }

    private void parseIntent() {
        sessionId = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        customization = (SessionCustomization) getIntent().getSerializableExtra(Extras.EXTRA_CUSTOMIZATION);

        if (customization != null) {
            addRightCustomViewOnActionBar(this, customization.buttons);
        }
    }

    // 添加action bar的右侧按钮及响应事件
    private void addRightCustomViewOnActionBar(UI activity, List<SessionCustomization.OptionsButton> buttons) {
        if (buttons == null || buttons.size() == 0 || mLlTitleRight == null) {
            return;
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ScreenUtil.dip2px(36), LinearLayout.LayoutParams.MATCH_PARENT);
        for (final SessionCustomization.OptionsButton button : buttons) {
            ImageView imageView = new ImageView(activity);
            imageView.setImageResource(button.iconId);
            imageView.setPadding(ScreenUtil.dip2px(6), 0, ScreenUtil.dip2px(6), 0);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    button.onClick(BaseMessageActivity.this, v, sessionId);
                }
            });
            mLlTitleRight.addView(imageView, params);
        }
    }

    protected Uri getIntentData() {
        return getIntent().getData();
    }
}

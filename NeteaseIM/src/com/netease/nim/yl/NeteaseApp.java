package com.netease.nim.yl;

import android.content.Context;
import android.text.TextUtils;

import com.netease.nim.avchatkit.AVChatKit;
import com.netease.nim.avchatkit.config.AVChatOptions;
import com.netease.nim.avchatkit.model.ITeamDataProvider;
import com.netease.nim.avchatkit.model.IUserInfoProvider;
import com.netease.nim.rtskit.RTSKit;
import com.netease.nim.rtskit.api.config.RTSOptions;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.UIKitOptions;
import com.netease.nim.uikit.business.contact.core.query.PinYin;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.yl.common.util.LogHelper;
import com.netease.nim.yl.config.preference.Preferences;
import com.netease.nim.yl.config.preference.UserPreferences;
import com.netease.nim.yl.contact.ContactHelper;
import com.netease.nim.yl.event.DemoOnlineStateContentProvider;
import com.netease.nim.yl.mixpush.DemoMixPushMessageHandler;
import com.netease.nim.yl.mixpush.DemoPushContentProvider;
import com.netease.nim.yl.rts.RTSHelper;
import com.netease.nim.yl.session.NimDemoLocationProvider;
import com.netease.nim.yl.session.SessionHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.mixpush.NIMPushClient;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.nimlib.sdk.util.NIMUtil;
import com.tencent.smtt.sdk.QbSdk;


public class NeteaseApp {
    /**
     * 初始化
     * @param context
     * @param backCls
     * @param optionCls
     */
    public static void neteaseIMInit(Context context,String baseUrl, Class backCls,Class optionCls) {
        QbSdk.initX5Environment(context,  null);
        StorageUtil.init(context, null);
        ScreenUtil.init(context);
        NetIMCache.setContext(context);
        NetIMCache.setHttpBaseUrl(baseUrl);
        NetIMCache.setBackActivity(backCls);
        // 4.6.0 开始，第三方推送配置入口改为 SDKOption#mixPushConfig，旧版配置方式依旧支持。
        NIMClient.init(context, getLoginInfo(), NimSDKOptionConfig.getSDKOptions(context, optionCls));
        // 以下逻辑只在主进程初始化时执行
        if (NIMUtil.isMainProcess(context)) {
            // 注册自定义推送消息处理，这个是可选项
            NIMPushClient.registerMixPushMessageHandler(new DemoMixPushMessageHandler());
            // init pinyin
            PinYin.init(context);
            PinYin.validate();
            // 初始化UIKit模块
            initUIKit(context);
            // 初始化消息提醒
            NIMClient.toggleNotification(UserPreferences.getNotificationToggle());
            // 云信sdk相关业务初始化
            NimInitManager.getInstance().init(true);
            // 初始化音视频模块
            initAVChatKit(optionCls);
            initRTSKit();
        }
    }

    private static void initUIKit(Context context) {
        // 初始化
        NimUIKit.init(context, buildUIKitOptions(context));
        // 设置地理位置提供者。如果需要发送地理位置消息，该参数必须提供。如果不需要，可以忽略。
        NimUIKit.setLocationProvider(new NimDemoLocationProvider());
        // IM 会话窗口的定制初始化。
        SessionHelper.init();
        // 聊天室聊天窗口的定制初始化。
        // 通讯录列表定制初始化
        ContactHelper.init();
        // 添加自定义推送文案以及选项，请开发者在各端（Android、IOS、PC、Web）消息发送时保持一致，以免出现通知不一致的情况
        NimUIKit.setCustomPushContentProvider(new DemoPushContentProvider());
        NimUIKit.setOnlineStateContentProvider(new DemoOnlineStateContentProvider());
    }

    private static LoginInfo getLoginInfo() {
        String account = Preferences.getUserAccount();
        String token = Preferences.getUserToken();
        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            NetIMCache.setAccount(account.toLowerCase());
            NetIMCache.setToken(token);
            return new LoginInfo(account, token);
        } else {
            return null;
        }
    }



    private static UIKitOptions buildUIKitOptions(Context context) {
        UIKitOptions options = new UIKitOptions();
        // 设置app图片/音频/日志等缓存目录
        options.appCacheDir = NimSDKOptionConfig.getAppCacheDir(context) + "/app";
        return options;
    }

    private static void initAVChatKit(Class optionCls) {
        AVChatOptions avChatOptions = new AVChatOptions() {
            @Override
            public void logout(Context context) {
                //MainActivity.logout(context, true);
            }
        };
        avChatOptions.entranceActivity = optionCls;
        avChatOptions.notificationIconRes = R.drawable.ic_stat_notify_msg;
        AVChatKit.init(avChatOptions);

        // 初始化日志系统
        LogHelper.init();
        // 设置用户相关资料提供者
        AVChatKit.setUserInfoProvider(new IUserInfoProvider() {
            @Override
            public UserInfo getUserInfo(String account) {
                return NimUIKit.getUserInfoProvider().getUserInfo(account);
            }

            @Override
            public String getUserDisplayName(String account) {
                return UserInfoHelper.getUserDisplayName(account);
            }
        });
        // 设置群组数据提供者
        AVChatKit.setTeamDataProvider(new ITeamDataProvider() {
            @Override
            public String getDisplayNameWithoutMe(String teamId, String account) {
                return TeamHelper.getDisplayNameWithoutMe(teamId, account);
            }

            @Override
            public String getTeamMemberDisplayName(String teamId, String account) {
                return TeamHelper.getTeamMemberDisplayName(teamId, account);
            }
        });
    }

    private static void initRTSKit() {
        RTSOptions rtsOptions = new RTSOptions() {
            @Override
            public void logout(Context context) {
                //MainActivity.logout(context, true);
            }
        };
        RTSKit.init(rtsOptions);
        RTSHelper.init();
    }
}

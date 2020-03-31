package com.netease.nim.yl;

import android.app.Activity;
import android.content.Context;

import com.netease.nim.avchatkit.AVChatKit;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusBarNotificationConfig;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

/**
 * Created by jezhee on 2/20/15.
 */
public class NetIMCache {
    /**
     * 设置 会话 返回的 Activity
     */
    private static Class<? extends Activity> backActivity;

    private static String httpBaseUrl;

    private static Context context;
    private static String account;

    //云信服务 token
    private static String token;

    //视频云点播服务 token
    private static String vodtoken;

    private static NimUserInfo userInfo;

    private static StatusBarNotificationConfig notificationConfig;

    public static void clear() {
        account = null;
    }

    public static String getAccount() {
        return account;
    }

    public static Class<? extends Activity> getBackActivity() {
        return backActivity;
    }

    public static void setHttpBaseUrl(String httpBaseUrl) {
        NetIMCache.httpBaseUrl = httpBaseUrl;
    }

    public static String getHttpBaseUrl() {
       return httpBaseUrl;
    }

    public static void setBackActivity(Class backActivity) {
        NetIMCache.backActivity = backActivity;
    }

    private static boolean mainTaskLaunching;

    public static void setAccount(String account) {
        NetIMCache.account = account;
        NimUIKit.setAccount(account);
        AVChatKit.setAccount(account);
    }

    public static void setNotificationConfig(StatusBarNotificationConfig notificationConfig) {
        NetIMCache.notificationConfig = notificationConfig;
    }

    public static StatusBarNotificationConfig getNotificationConfig() {
        return notificationConfig;
    }

    public static void setContext(Context context) {
        NetIMCache.context = context.getApplicationContext();
        AVChatKit.setContext(context);
    }

    public static void setMainTaskLaunching(boolean mainTaskLaunching) {
        NetIMCache.mainTaskLaunching = mainTaskLaunching;
        AVChatKit.setMainTaskLaunching(mainTaskLaunching);
    }

    public static boolean isMainTaskLaunching() {
        return mainTaskLaunching;
    }
    public static Context getContext() {
        return context;
    }

    public static NimUserInfo getUserInfo() {
        if (userInfo == null) {
            userInfo = NIMClient.getService(UserService.class).getUserInfo(account);
        }
        return userInfo;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        NetIMCache.token = token;
    }

}

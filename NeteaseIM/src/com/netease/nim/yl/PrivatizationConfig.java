package com.netease.nim.yl;

import com.netease.nimlib.sdk.ServerAddresses;

/**
 * 网易云信私有化配置项
 */
public class PrivatizationConfig {

    public static ServerAddresses getServerAddresses() {
        return null;
    }

    public static String getAppKey() {
        return null;
    }

    private static ServerAddresses get() {
        ServerAddresses addresses = new ServerAddresses();
        addresses.nosDownload = "nos.netease.com";
        addresses.nosAccess = "{bucket}.nosdn.127.net/{object}";
        return addresses;
    }
}

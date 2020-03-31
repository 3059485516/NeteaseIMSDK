package com.netease.nim.yl.interactlive.entertainment.utils;

import android.content.Context;
import android.net.TrafficStats;


public class NetWorkSpeedUtils {
    private Context mContext;

    private long mLastTotalRxBytes = 0;
    private long mLastTimeStamp = 0;

    public NetWorkSpeedUtils(Context context) {
        mContext = context;
        mLastTotalRxBytes = getTotalRxBytes();
        mLastTimeStamp = System.currentTimeMillis();
    }

    private long getTotalRxBytes() {
        return TrafficStats.getUidRxBytes(mContext.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);
    }

    /**
     * 获取网络质量
     *
     * @return
     */
    public NetConnectionQuality getNetSpeed() {
        long nowTotalRxBytes = getTotalRxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - mLastTotalRxBytes) * 1000 / (nowTimeStamp - mLastTimeStamp));
        mLastTimeStamp = nowTimeStamp;
        mLastTotalRxBytes = nowTotalRxBytes;
        if (speed <= 30) {
            return NetConnectionQuality.BAD;
        } else if (speed <= 70) {
            return NetConnectionQuality.POOR;
        } else if (speed <= 150) {
            return NetConnectionQuality.GOOD;
        } else {
            return NetConnectionQuality.EXCELLENT;
        }
    }
}

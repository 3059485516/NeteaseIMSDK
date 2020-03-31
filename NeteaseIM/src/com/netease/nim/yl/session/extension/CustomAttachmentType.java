package com.netease.nim.yl.session.extension;

/**
 * Created by zhoujianghua on 2015/4/9.
 */
public interface CustomAttachmentType {
    // 多端统一
    int Guess = 1;
    int SnapChat = 2;
    int Sticker = 3;
    int RTS = 4;
    int RedPacket = 5;
    int OpenedRedPacket = 6;
    int gift = 7; // 礼物
    int like = 8; // 点赞
    int connectedMic = 9; // 同意互动连接
    int disconnectMic = 10; // 断开互动连接
}

package com.netease.nim.yl.session.extension;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.yl.interactlive.entertainment.module.ConnectedAttachment;
import com.netease.nim.yl.interactlive.entertainment.module.DisconnectAttachment;
import com.netease.nim.yl.interactlive.entertainment.module.GiftAttachment;
import com.netease.nim.yl.interactlive.entertainment.module.LikeAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser;

/**
 * Created by zhoujianghua on 2015/4/9.
 */
public class CustomAttachParser implements MsgAttachmentParser {

    private static final String KEY_TYPE = "type";
    private static final String KEY_DATA = "data";

    @Override
    public MsgAttachment parse(String json) {
        CustomAttachment attachment = null;
        try {
            JSONObject object = JSON.parseObject(json);
            int type = object.getInteger(KEY_TYPE);
            JSONObject data = object.getJSONObject(KEY_DATA);
            if (type == CustomAttachmentType.Guess) {
                attachment = new GuessAttachment();

            } else if (type == CustomAttachmentType.SnapChat) {
                return new SnapChatAttachment(data);
            } else if (type == CustomAttachmentType.Sticker) {
                attachment = new StickerAttachment();

            } else if (type == CustomAttachmentType.RTS) {
                attachment = new RTSAttachment();

            } else if (type == CustomAttachmentType.RedPacket) {
                attachment = new RedPacketAttachment();

            } else if (type == CustomAttachmentType.OpenedRedPacket) {
                attachment = new RedPacketOpenedAttachment();

            } else if (type == CustomAttachmentType.gift) {
                attachment = new GiftAttachment();

            } else if (type == CustomAttachmentType.like) {
                attachment = new LikeAttachment();

            } else if (type == CustomAttachmentType.connectedMic) {
                attachment = new ConnectedAttachment();

            } else if (type == CustomAttachmentType.disconnectMic) {
                attachment = new DisconnectAttachment();

            } else {
                attachment = new DefaultCustomAttachment();

            }

            if (attachment != null) {
                attachment.fromJson(data);
            }
        } catch (Exception e) {

        }

        return attachment;
    }

    public static String packData(int type, JSONObject data) {
        JSONObject object = new JSONObject();
        object.put(KEY_TYPE, type);
        if (data != null) {
            object.put(KEY_DATA, data);
        }

        return object.toJSONString();
    }
}

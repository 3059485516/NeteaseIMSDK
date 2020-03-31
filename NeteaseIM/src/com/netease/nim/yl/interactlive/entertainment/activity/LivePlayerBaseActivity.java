package com.netease.nim.yl.interactlive.entertainment.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.session.emoji.MoonUtil;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.business.session.module.ModuleProxy;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.impl.cache.ChatRoomMemberCache;
import com.netease.nim.yl.NetIMCache;
import com.netease.nim.yl.R;
import com.netease.nim.yl.interactlive.base.TActivity;
import com.netease.nim.yl.interactlive.entertainment.adapter.GiftAdapter;
import com.netease.nim.yl.interactlive.entertainment.adapter.MemberAdapter;
import com.netease.nim.yl.interactlive.entertainment.constant.GiftType;
import com.netease.nim.yl.interactlive.entertainment.constant.LiveType;
import com.netease.nim.yl.interactlive.entertainment.constant.PushLinkConstant;
import com.netease.nim.yl.interactlive.entertainment.constant.PushMicNotificationType;
import com.netease.nim.yl.interactlive.entertainment.helper.GiftAnimation;
import com.netease.nim.yl.interactlive.entertainment.module.ChatRoomMsgListPanel;
import com.netease.nim.yl.interactlive.entertainment.module.ConnectedAttachment;
import com.netease.nim.yl.interactlive.entertainment.module.DisconnectAttachment;
import com.netease.nim.yl.interactlive.entertainment.module.GiftAttachment;
import com.netease.nim.yl.interactlive.entertainment.module.LikeAttachment;
import com.netease.nim.yl.interactlive.im.session.input.InputConfig;
import com.netease.nim.yl.interactlive.im.session.input.InputPanel;
import com.netease.nim.yl.interactlive.im.ui.periscope.PeriscopeLayout;
import com.netease.nim.yl.interactlive.permission.MPermission;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.avchat.AVChatStateObserver;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.model.AVChatSurfaceViewRenderer;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.ChatRoomServiceObserver;
import com.netease.nimlib.sdk.chatroom.constant.MemberQueryType;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomKickOutEvent;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomNotificationAttachment;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomStatusChangeData;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomData;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomResultData;
import com.netease.nimlib.sdk.chatroom.model.MemberOption;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 直播端和观众端的基类
 * Created by hzxuwen on 2016/4/5.
 */
public abstract class LivePlayerBaseActivity extends TActivity implements ModuleProxy, AVChatStateObserver {
    private static final String TAG = LivePlayerBaseActivity.class.getSimpleName();

    protected final int LIVE_PERMISSION_REQUEST_CODE = 100;
    protected final static String EXTRA_MEETING_NAME = "EXTRA_MEETING_NAME";
    ////聊天室ID
    protected final static String EXTRA_LIVE_CID = "LIVE_CID";
    protected final static String EXTRA_ROOM_ID = "ROOM_ID";
    protected final static String EXTRA_ROOM_MASTER = "ROOM_MASTER";
    protected final static String EXTRA_URL = "EXTRA_URL";
    protected final static String EXTRA_MODE = "EXTRA_MODE";
    //推流地址
    protected final static String EXTRA_PUSH_URL = "EXTRA_PUSH_URL";
    //拉流地址
    protected final static String EXTRA_PULL_URL = "EXTRA_PULL_URL";
    protected final static String EXTRA_CREATOR = "EXTRA_CREATOR";
    private final static int FETCH_ONLINE_PEOPLE_COUNTS_DELTA = 8 * 1000;
    protected final static String AVATAR_DEFAULT = "avatar_default";

    private Timer timer;

    // 聊天室信息
    protected String roomId;
    protected ChatRoomInfo roomInfo;
    protected String pullUrl; // 拉流地址
    protected String masterNick; // 主播昵称
    protected String meetingName; // 音视频会议房间名称
    protected String phone; // 音视频会议房间名称
    protected boolean isCreator; // 是否是主播
    protected int screenOrientation; //屏幕方向

    // modules
    protected InputPanel inputPanel;
    protected ChatRoomMsgListPanel messageListPanel;
    private EditText messageEditText;

    // view
    protected ViewGroup rootView;
    protected TextView masterNameText;
    protected HeadImageView masterNameImg;
    private TextView inputBtn; //文字模式按钮
    private TextView onlineCountText; // 在线人数view
    protected GridView giftView; // 礼物列表
    private RelativeLayout giftAnimationViewDown; // 礼物动画布局1
    private RelativeLayout giftAnimationViewUp; // 礼物动画布局2
    protected PeriscopeLayout periscopeLayout; // 点赞爱心布局
    protected ImageButton giftBtn; // 礼物按钮
    protected ViewGroup giftLayout; // 礼物布局
    protected LinearLayout controlContainer; // 右下角几个image button布局
    protected ViewGroup roomOwnerLayout; // master名称布局
    protected ViewGroup roomNameLayout; //房间名称
    protected TextView roomName; //房间名
    protected TextView interactionBtn; // 互动按钮
    protected TextView fakeListText; // 占坑用的view，message listview可以浮动上下
    protected RelativeLayout connectionViewLayout; // 连麦画面布局
    protected TextView loadingNameText; // 连麦的观众姓名，等待中的画面
    protected TextView onMicNameText; // 连麦的观众姓名
    protected ViewGroup audienceLoadingLayout; // 连麦观众等待画面
    protected ViewGroup audienceLivingLayout; // 连麦观众正在播放画面
    protected TextView livingBg; // 防止用户关闭权限，没有图像时显示
    protected View connectionViewCloseBtn; // 关闭连麦画面按钮
    protected ViewGroup connectionCloseConfirmLayout; // 连麦关闭确认画面
    private TextView connectionCloseConfirmTipsTv;
    protected TextView connectionCloseConfirm; // 连麦关闭确认
    protected TextView connectionCloseCancel; // 连麦关闭取消
    protected TextView loadingClosingText; // 正在连接/已关闭文案
    protected ViewGroup videoModeBgLayout; // 主播画面视频模式背景
    protected ViewGroup audioModeBgLayout; // 主播画面音频模式背景
    protected ViewGroup audioModeBypassLayout; // 音频模式旁路直播画面
    protected AVChatSurfaceViewRenderer bypassVideoRender; // 旁路直播画面
    //新增的View
    protected RecyclerView mRvMember;
    protected RelativeLayout mRlMemberInfo;
    protected ImageView mIvMemberInfoClose;
    protected HeadImageView mIvMemberInfoHead;
    protected TextView mTvMemberInfoName;
    protected TextView mTvMemberInfoPhone;
    protected Button mBtnAnchorLinkVideo;
    protected Button mBtnAnchorLinkAudio;
    protected Button mBtnBannedMembers;

    //用户进出房间的提醒
    protected LinearLayout mLlMemberInto;
    protected HeadImageView mIvIntoMemberHead;
    protected TextView mTvIntoMemberName;

    protected LinearLayout mLlMemberLeave;
    protected HeadImageView mIvLeaveMemberHead;
    protected TextView mTvLeaveMemberName;
    // data
    protected GiftAdapter adapter;
    protected GiftAnimation giftAnimation; // 礼物动画
    private AbortableFuture<EnterChatRoomResultData> enterRequest;
    protected String onMicNick;  // 连麦用户昵称
    // state
    protected boolean isOnMic = false; // 是否连上麦
    protected boolean isMeOnMic = false; // 是否我自己连上麦
    protected LiveType liveType; // 直播类型
    protected int style; // 语音/视频连麦类型
    protected boolean isDestroyed = false;

    protected abstract int getActivityLayout(); // activity布局文件

    protected abstract int getLayoutId(); // 根布局资源id

    protected abstract int getControlLayout(); // 控制按钮布局

    private AlertDialog mNetworkDialog;
    protected MemberAdapter mMemberAdapter;

    //当前用户是否被禁言
    private boolean mIsMute = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(getActivityLayout());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //应用运行时，保持屏幕高亮，不锁屏
        parseIntent();
        // 注册监听
        registerObservers(true);
        registerNetTypeChangeObserver();
    }

    protected void parseIntent() {
        isCreator = getIntent().getBooleanExtra(EXTRA_CREATOR, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (messageListPanel != null) {
            messageListPanel.onResume();
        }
    }

    @Override
    public void onBackPressed() {
        if (inputPanel != null) {
            inputPanel.collapse(true);
        }
        if (messageListPanel != null) {
            messageListPanel.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        unRegisterNetTypeChangeObserver();
        isDestroyed = true;
        super.onDestroy();
        registerObservers(false);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        adapter = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(getActivityLayout());
        controlContainer.removeAllViews();
        findViews();
    }

    /***********************
     * 录音摄像头权限申请
     *******************************/

    // 权限控制
    protected static final String[] LIVE_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE};

    protected void requestLivePermission() {
        MPermission.with(this).addRequestCode(LIVE_PERMISSION_REQUEST_CODE).permissions(LIVE_PERMISSIONS).request();
    }

    /***************************
     * 监听
     ****************************/

    private void registerObservers(boolean register) {
        NIMClient.getService(ChatRoomServiceObserver.class).observeReceiveMessage(incomingChatRoomMsg, register);
        NIMClient.getService(ChatRoomServiceObserver.class).observeOnlineStatus(onlineStatus, register);
        NIMClient.getService(ChatRoomServiceObserver.class).observeKickOutEvent(kickOutObserver, register);
        NIMClient.getService(MsgServiceObserve.class).observeCustomNotification(customNotification, register);
        NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(userStatusObserver, register);
    }

    Observer<List<ChatRoomMessage>> incomingChatRoomMsg = new Observer<List<ChatRoomMessage>>() {
        @Override
        public void onEvent(List<ChatRoomMessage> messages) {
            Log.d("ChatRoomMessage", "ssssssssssssssssssssss");
            if (messages == null || messages.isEmpty()) {
                return;
            }
            for (ChatRoomMessage message : messages) {
                if (message != null && message.getAttachment() instanceof GiftAttachment) {
                    // 收到礼物消息
                    GiftType type = ((GiftAttachment) message.getAttachment()).getGiftType();
                    updateGiftList(type);
                    giftAnimation.showGiftAnimation(message);
                } else if (message != null && message.getAttachment() instanceof LikeAttachment) {
                    // 收到点赞爱心
                    periscopeLayout.addHeart();
                } else if (message != null && message.getAttachment() instanceof ChatRoomNotificationAttachment) {
                    // 通知类消息
                    ChatRoomNotificationAttachment attachment = (ChatRoomNotificationAttachment) message.getAttachment();
                    if (attachment.getType() == NotificationType.ChatRoomMemberIn) {    //有人进入房间
                        getLiveMode(attachment.getExtension());
                        KLog.d(TAG, attachment.getOperatorNick() + " into the room");
                        if (NetIMCache.getAccount() != null && !NetIMCache.getAccount().equals(attachment.getOperator())) {
                            mQueueIn.add(attachment);
                            userIntoRoomAnim();
                        }
                    } else if (attachment.getType() == NotificationType.ChatRoomMemberExit) {    //有人离开房间
                        KLog.d(TAG, attachment.getOperatorNick() + " leave the room");
                        mQueueOut.add(attachment);
                        userLeaveRoomAnim();
                    } else if (attachment.getType() == NotificationType.ChatRoomInfoUpdated) {
                        onReceiveChatRoomInfoUpdate(attachment.getExtension());
                    }
                } else if (message != null && message.getAttachment() instanceof ConnectedAttachment) {
                    // 观众收到旁路直播连接消息
                    onMicConnectedMsg(message);
                } else if (message != null && message.getAttachment() instanceof DisconnectAttachment) {
                    // 观众收到旁路直播断开消息
                    LogUtil.i(TAG, "disconnect");
                    DisconnectAttachment attachment = (DisconnectAttachment) message.getAttachment();
                    if (!TextUtils.isEmpty(attachment.getAccount()) && attachment.getAccount().equals(roomInfo.getCreator())) {
                        resetConnectionView();
                    } else {
                        onMicDisConnectedMsg(attachment.getAccount());
                    }
                } else {
                    messageListPanel.onIncomingMessage(message);
                }
            }
        }
    };

    Observer<CustomNotification> customNotification = new Observer<CustomNotification>() {
        @Override
        public void onEvent(CustomNotification customNotification) {
            if (customNotification == null) {
                return;
            }
            String content = customNotification.getContent();
            try {
                JSONObject json = JSON.parseObject(content);
                String fromRoomId = json.getString(PushLinkConstant.roomid);
                if (!roomId.equals(fromRoomId)) {
                    return;
                }
                int id = json.getIntValue(PushLinkConstant.command);
                Log.i(TAG, "receive command type:" + id + "----" + customNotification.getFromAccount());
                if (id == PushMicNotificationType.JOIN_QUEUE.getValue()) {
                    // 加入连麦队列
                    joinQueue(customNotification, json);
                } else if (id == PushMicNotificationType.EXIT_QUEUE.getValue()) {
                    // 退出连麦队列
                    exitQueue(customNotification);
                } else if (id == PushMicNotificationType.CONNECTING_MIC.getValue()) {
                    // 主播选中某人连麦
                    onMicLinking(json);
                } else if (id == PushMicNotificationType.DISCONNECT_MIC.getValue()) {
                    // 被主播断开连麦
                    onMicCanceling();
                } else if (id == PushMicNotificationType.REJECT_CONNECTING.getValue()) {
                    // 观众由于重新进入了房间而拒绝连麦
                    rejectConnecting(customNotification.getFromAccount());
                }
            } catch (Exception e) {
                LogUtil.e(TAG, e.toString());
            }
        }
    };

    Observer<ChatRoomStatusChangeData> onlineStatus = new Observer<ChatRoomStatusChangeData>() {
        @Override
        public void onEvent(ChatRoomStatusChangeData chatRoomStatusChangeData) {
            if (chatRoomStatusChangeData.status == StatusCode.CONNECTING) {
                DialogMaker.updateLoadingMessage("连接中...");
            } else if (chatRoomStatusChangeData.status == StatusCode.UNLOGIN) {
                onOnlineStatusChanged(false);
                /*Toast.makeText(LivePlayerBaseActivity.this, R.string.nim_status_unlogin, Toast.LENGTH_SHORT).show();*/
            } else if (chatRoomStatusChangeData.status == StatusCode.LOGINING) {
                DialogMaker.updateLoadingMessage("登录中...");
            } else if (chatRoomStatusChangeData.status == StatusCode.LOGINED) {
                onOnlineStatusChanged(true);
            } else if (chatRoomStatusChangeData.status == StatusCode.NET_BROKEN) {
                onOnlineStatusChanged(false);
                Toast.makeText(LivePlayerBaseActivity.this, R.string.net_broken, Toast.LENGTH_SHORT).show();
            }
            LogUtil.i(TAG, "Chat Room Online Status:" + chatRoomStatusChangeData.status.name());
        }
    };

    /**
     * 用户状态变化
     */
    Observer<StatusCode> userStatusObserver = new Observer<StatusCode>() {

        @Override
        public void onEvent(StatusCode code) {
            if (code.wontAutoLogin()) {
                clearChatRoom();
            }
        }
    };

    Observer<ChatRoomKickOutEvent> kickOutObserver = new Observer<ChatRoomKickOutEvent>() {
        @Override
        public void onEvent(ChatRoomKickOutEvent chatRoomKickOutEvent) {
            Toast.makeText(LivePlayerBaseActivity.this, "被踢出聊天室，原因:" + chatRoomKickOutEvent.getReason(), Toast.LENGTH_SHORT).show();
            clearChatRoom();
        }
    };


    /**************************
     * 断网重连
     ****************************/

    protected void onOnlineStatusChanged(boolean isOnline) {
        if (isOnline) {
            onConnected();
        } else {
            onDisconnected();
        }
    }

    protected abstract void onConnected(); // 网络连上

    protected abstract void onDisconnected(); // 网络断开

    /****************************
     * 布局初始化
     **************************/
    protected void findViews() {
        //成员列表
        mRvMember = findView(R.id.rv_member);
        mRvMember.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mRvMember.setAdapter(mMemberAdapter = new MemberAdapter());
        mMemberAdapter.setItemClickListener(new MemberAdapter.ItemClickListener() {
            @Override
            public void itemClick(final ChatRoomMember chatRoomMember) {
                if (mRlMemberInfo.getVisibility() == View.GONE) {
                    mRlMemberInfo.setVisibility(View.VISIBLE);
                }
                if (mBtnBannedMembers != null) {
                    if (NetIMCache.getAccount().equals(chatRoomMember.getAccount())) {
                        mBtnBannedMembers.setVisibility(View.GONE);
                    } else {
                        mBtnBannedMembers.setVisibility(View.VISIBLE);
                    }
                    final boolean isMuted = chatRoomMember.isMuted();
                    //判断用户是否被禁言
                    if (isMuted) {
                        mBtnBannedMembers.setText("取消禁言");
                    } else {
                        mBtnBannedMembers.setText("禁言");
                    }
                    mBtnBannedMembers.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bannedChatRoomMembers(chatRoomMember.getRoomId(), chatRoomMember.getAccount(), isMuted);
                        }
                    });
                }

                mIvMemberInfoHead.loadAvatar(chatRoomMember.getAvatar());
                mTvMemberInfoName.setText(chatRoomMember.getNick());
                mTvMemberInfoPhone.setText(chatRoomMember.getAccount());
                if (mBtnAnchorLinkVideo != null) {
                    mBtnAnchorLinkVideo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mRlMemberInfo.setVisibility(View.GONE);
                            anchorLinkVideo(chatRoomMember);
                        }
                    });
                    if (NetIMCache.getAccount().equals(chatRoomMember.getAccount())) {
                        mBtnAnchorLinkVideo.setVisibility(View.GONE);
                    } else {
                        mBtnAnchorLinkVideo.setVisibility(View.VISIBLE);
                    }
                }

                if (mBtnAnchorLinkAudio != null) {
                    mBtnAnchorLinkAudio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mRlMemberInfo.setVisibility(View.GONE);
                            anchorLinkAudio(chatRoomMember);
                        }
                    });
                    if (NetIMCache.getAccount().equals(chatRoomMember.getAccount())) {
                        mBtnAnchorLinkAudio.setVisibility(View.GONE);
                    } else {
                        mBtnAnchorLinkAudio.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        //成员信息
        mRlMemberInfo = findView(R.id.rl_member_info);
        mIvMemberInfoClose = findView(R.id.iv_member_info_close);
        mIvMemberInfoHead = findView(R.id.iv_member_info_head);
        mTvMemberInfoName = findView(R.id.tv_member_info_name);
        mTvMemberInfoPhone = findView(R.id.tv_member_info_phone);
        mBtnAnchorLinkVideo = findView(R.id.btn_anchor_link_video);
        mBtnAnchorLinkAudio = findView(R.id.btn_anchor_link_audio);
        mBtnBannedMembers = findView(R.id.btn_banned_members);
        mIvMemberInfoClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRlMemberInfo.setVisibility(View.GONE);
            }
        });

        mLlMemberInto = findView(R.id.ll_member_into);
        mIvIntoMemberHead = findView(R.id.iv_into_member_head);
        mTvIntoMemberName = findView(R.id.tv_into_member_name);

        mLlMemberLeave = findView(R.id.ll_member_leave);
        mIvLeaveMemberHead = findView(R.id.iv_leave_member_head);
        mTvLeaveMemberName = findView(R.id.tv_leave_member_name);

        initAnim();

        roomName = findView(R.id.room_name);
        masterNameText = findView(R.id.master_name);
        masterNameImg = findView(R.id.master_head);
        onlineCountText = findView(R.id.online_count_text);
        roomOwnerLayout = findView(R.id.room_owner_layout);
        roomNameLayout = findView(R.id.room_name_layout);
        //控制布局
        findControlViews();
        interactionBtn = findView(R.id.interaction_btn);
        giftBtn = findView(R.id.gift_btn);

        // 礼物列表
        findGiftLayout();
        // 点赞的爱心布局
        periscopeLayout = findViewById(R.id.periscope);
        // 互动连麦布局
        findInteractionViews();
    }

    /**
     * 禁言或者 解禁用户 发言
     *
     * @param roomId
     * @param account
     */
    private void bannedChatRoomMembers(String roomId, String account, final boolean isMuted) {
        MemberOption option = new MemberOption(roomId, account);
        NIMClient.getService(ChatRoomService.class).markChatRoomMutedList(!isMuted, option).setCallback(new RequestCallback<ChatRoomMember>() {
            @SuppressLint("ShowToast")
            @Override
            public void onSuccess(ChatRoomMember param) {
                // 成功
                String name;
                if (isMuted) {
                    name = "解除禁言成功!";
                } else {
                    name = "禁言成功!";
                }
                if (mRlMemberInfo != null){
                    mRlMemberInfo.setVisibility(View.GONE);
                }
                Toast.makeText(LivePlayerBaseActivity.this, name, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code) {
                Toast.makeText(LivePlayerBaseActivity.this, "禁言失败：" + code, Toast.LENGTH_SHORT).show();
            }

            @SuppressLint("ShowToast")
            @Override
            public void onException(Throwable exception) {
                Toast.makeText(LivePlayerBaseActivity.this, "禁言失败：" + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void anchorLinkVideo(ChatRoomMember chatRoomMember) {
    }

    protected void anchorLinkAudio(ChatRoomMember chatRoomMember) {
    }

    protected void findControlViews() {
        controlContainer = findView(R.id.control_container);
        View view = LayoutInflater.from(this).inflate(getControlLayout(), null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        controlContainer.addView(view, lp);
    }

    protected void findInputViews() {
        Container container = new Container(this, roomId, SessionTypeEnum.ChatRoom, this);
        View view = findViewById(getLayoutId());
        if (messageListPanel == null) {
            messageListPanel = new ChatRoomMsgListPanel(container, view);
        }
        InputConfig inputConfig = new InputConfig();
        inputConfig.isTextAudioSwitchShow = false;
        inputConfig.isMoreFunctionShow = false;
        inputConfig.isEmojiButtonShow = false;
        if (inputPanel == null) {
            inputPanel = new InputPanel(container, view, getActionList(), inputConfig);
        } else {
            inputPanel.reload(container, inputConfig);
        }
        messageEditText = findView(R.id.editTextMessage);

        inputBtn = findView(R.id.input_btn);
        inputBtn.setOnClickListener(baseClickListener);
        inputPanel.hideInputPanel();
        inputPanel.collapse(true);
    }

    // 初始化礼物布局
    protected void findGiftLayout() {
        giftLayout = findView(R.id.gift_layout);
        giftView = findView(R.id.gift_grid_view);

        giftAnimationViewDown = findView(R.id.gift_animation_view);
        giftAnimationViewUp = findView(R.id.gift_animation_view_up);
        giftAnimation = new GiftAnimation(giftAnimationViewDown, giftAnimationViewUp);
    }

    // 更新礼物列表，由子类定义
    protected void updateGiftList(GiftType type) {
    }

    // 互动连麦布局
    private void findInteractionViews() {
        audioModeBgLayout = findView(R.id.audio_mode_background);
        videoModeBgLayout = findView(R.id.video_layout);
        bypassVideoRender = findView(R.id.bypass_video_render);
        bypassVideoRender.setZOrderMediaOverlay(true);
        showModeLayout();
        connectionViewLayout = findView(R.id.interaction_view_layout);
        loadingNameText = findView(R.id.loading_name);
        onMicNameText = findView(R.id.on_mic_name);
        audienceLoadingLayout = findView(R.id.audience_loading_layout);
        audienceLivingLayout = findView(R.id.audience_living_layout);
        livingBg = findView(R.id.no_video_bg);
        connectionViewCloseBtn = findView(R.id.interaction_close_btn);
        connectionCloseConfirmLayout = findView(R.id.interaction_close_confirm_layout);
        connectionCloseConfirmTipsTv = findView(R.id.interaction_close_confirm_tips_tv);
        connectionCloseConfirm = findView(R.id.close_confirm);
        connectionCloseCancel = findView(R.id.close_cancel);
        loadingClosingText = findView(R.id.loading_closing_text);
        audioModeBypassLayout = findView(R.id.audio_mode_audience_layout);

        connectionViewCloseBtn.setOnClickListener(baseClickListener);
        connectionCloseConfirm.setOnClickListener(baseClickListener);
        connectionCloseCancel.setOnClickListener(baseClickListener);
    }

    // 连麦布局显示
    protected void showModeLayout() {
        if (liveType == LiveType.VIDEO_TYPE) {
            videoModeBgLayout.setVisibility(View.VISIBLE);
            audioModeBgLayout.setVisibility(View.GONE);
        } else if (liveType == LiveType.AUDIO_TYPE) {
            videoModeBgLayout.setVisibility(View.GONE);
            audioModeBgLayout.setVisibility(View.VISIBLE);
        }
    }

    /****************************
     * 进入聊天室
     ***********************/

    // 进入聊天室
    public void enterRoom() {
        if (isDestroyed) {
            return;
        }
        DialogMaker.showProgressDialog(this, null, "", true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (enterRequest != null) {
                    enterRequest.abort();
                    onLoginDone();
                    finish();
                }
            }
        }).setCanceledOnTouchOutside(false);
        EnterChatRoomData data = new EnterChatRoomData(roomId);
        setEnterRoomExtension(data);
        enterRequest = NIMClient.getService(ChatRoomService.class).enterChatRoom(data);
        enterRequest.setCallback(new RequestCallback<EnterChatRoomResultData>() {
            @Override
            public void onSuccess(EnterChatRoomResultData result) {
                onLoginDone();
                roomInfo = result.getRoomInfo();
                ChatRoomMember member = result.getMember();
                member.setRoomId(roomInfo.getRoomId());
                ChatRoomMemberCache.getInstance().saveMyMember(member);
                Map<String, Object> ext = roomInfo.getExtension();
                getLiveMode(ext);
                updateUI();
                updateRoomUI(false);
            }
            @SuppressLint("ShowToast")
            @Override
            public void onFailed(int code) {
                onLoginDone();
                if (code == ResponseCode.RES_CHATROOM_BLACKLIST) {
                    Toast.makeText(LivePlayerBaseActivity.this, "你已被拉入黑名单，不能再进入", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LivePlayerBaseActivity.this, "进入房间失败", Toast.LENGTH_SHORT).show();
                }
                if (isCreator) {
                    finish();
                }
            }

            @SuppressLint("ShowToast")
            @Override
            public void onException(Throwable exception) {
                onLoginDone();
                Toast.makeText(LivePlayerBaseActivity.this, "进入房间异常" + exception.getMessage(), Toast.LENGTH_SHORT);
                if (isCreator) {
                    finish();
                }
            }
        });
    }

    // 主播将自己的模式放到进入聊天室的通知扩展中，告诉观众，由主播实现。
    protected void setEnterRoomExtension(EnterChatRoomData enterChatRoomData) {
    }

    // 获取当前直播的模式
    private void getLiveMode(Map<String, Object> ext) {
        LogUtil.d(TAG, "getLiveMode: ext == null: " + (ext == null));
        if (ext != null) {
            if (ext.containsKey(PushLinkConstant.type)) {
                Object typeObject = ext.get(PushLinkConstant.type);
                int type = 0;
                if (typeObject instanceof String) {
                    type = Integer.parseInt((String) typeObject);
                } else if (typeObject instanceof Integer) {
                    type = (int) typeObject;
                }
                liveType = LiveType.typeOfValue(type);
            }

            if (ext.containsKey(PushLinkConstant.meetingName)) {
                String temp = (String) ext.get(PushLinkConstant.meetingName);
                if (!TextUtils.isEmpty(temp)) {
                    meetingName = temp;
                }
                LogUtil.d(TAG, "getLiveMode: meetingName = " + meetingName);
            } else {
                LogUtil.d(TAG, "getLiveMode: meetingName == null");
            }

            if (ext.containsKey(PushLinkConstant.orientation)) {
                Object o = ext.get(PushLinkConstant.orientation);
                if(o instanceof  Integer) {
                    screenOrientation = (int) o;
                    if ((getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 1 : 2) != screenOrientation) {
                        setRequestedOrientation(screenOrientation == 1 ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                }
            }
        }
    }

    private void onLoginDone() {
        enterRequest = null;
        DialogMaker.dismissProgressDialog();
    }

    // 更新在线人数
    protected void updateUI() {
        roomName.setText(roomId);
        masterNameText.setText(roomInfo.getCreator());
        masterNameImg.loadBuddyAvatar(roomInfo.getCreator());
        onlineCountText.setText(String.format("%s人", String.valueOf(roomInfo.getOnlineUserCount())));
        fetchOnlineCount();
    }

    // 聊天室信息相关界面
    protected void updateRoomUI(boolean isHide) {
        if (isHide) {
            controlContainer.setVisibility(View.GONE);
            roomOwnerLayout.setVisibility(View.GONE);
            roomNameLayout.setVisibility(View.GONE);
        } else {
            controlContainer.setVisibility(View.VISIBLE);
            roomOwnerLayout.setVisibility(View.VISIBLE);
        }
    }

    // 一分钟轮询一次在线人数
    private void fetchOnlineCount() {
        if (timer == null) {
            timer = new Timer();
        }

        //开始一个定时任务
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getOnlineMemberInfo();
            }
        }, FETCH_ONLINE_PEOPLE_COUNTS_DELTA, FETCH_ONLINE_PEOPLE_COUNTS_DELTA);
    }

    /**
     * 查询聊天室在线人员
     */
    private void fetchOnlineRoomMembers(final List<ChatRoomMember> guestChatRoomMembers) {
        NIMClient.getService(ChatRoomService.class).fetchRoomMembers(roomId, MemberQueryType.ONLINE_NORMAL, 0, 10000).setCallback(new RequestCallback<List<ChatRoomMember>>() {
            @Override
            public void onSuccess(List<ChatRoomMember> chatRoomMembers) {
                if (mMemberAdapter != null) {
                    if (guestChatRoomMembers != null) {
                        guestChatRoomMembers.addAll(chatRoomMembers);
                        setMuteMembersStatus(guestChatRoomMembers);
                    }
                    mMemberAdapter.updateData(guestChatRoomMembers);
                }
            }

            @Override
            public void onFailed(int i) {
            }

            @Override
            public void onException(Throwable throwable) {
                LogUtil.d(TAG, "在线人数信息onException: ");
            }
        });
    }

    private void setMuteMembersStatus(List<ChatRoomMember> guestChatRoomMembers) {
        for (ChatRoomMember member : guestChatRoomMembers) {
            if (NetIMCache.getAccount().equals(member.getAccount())) {
                if (!mIsMute) {
                    mIsMute = member.isMuted();
                }
                break;
            }
        }
    }

    /**
     * 查询游客人员
     */
    private void fetchGuestRoomMembers() {
        NIMClient.getService(ChatRoomService.class).fetchRoomMembers(roomId, MemberQueryType.GUEST, 0, 10000).setCallback(new RequestCallback<List<ChatRoomMember>>() {
            @Override
            public void onSuccess(List<ChatRoomMember> chatRoomMembers) {
                if (mMemberAdapter != null) {
                    fetchOnlineRoomMembers(chatRoomMembers);
                }
            }

            @Override
            public void onFailed(int i) {
            }

            @Override
            public void onException(Throwable throwable) {
                LogUtil.d(TAG, "在线人数信息onException: ");
            }
        });
    }

    private void getOnlineMemberInfo() {
        mIsMute = false;
        fetchGuestRoomMembers();
        NIMClient.getService(ChatRoomService.class).fetchRoomInfo(roomId).setCallback(new RequestCallback<ChatRoomInfo>() {
            @Override
            public void onSuccess(final ChatRoomInfo param) {
                onlineCountText.setText(String.format("%s人", String.valueOf(param.getOnlineUserCount())));
                //判断聊天室是佛是整体禁言
                if (param.isMute()) {
                    mIsMute = true;
                }
            }

            @Override
            public void onFailed(int code) {
                LogUtil.d(TAG, "fetch room info failed:" + code);
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.d(TAG, "fetch room info exception:" + exception);
            }
        });
    }

    /*******************
     * 离开聊天室
     ***********************/
    private void clearChatRoom() {
        ChatRoomMemberCache.getInstance().clearRoomCache(roomId);
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 50);
    }


    protected void onReceiveChatRoomInfoUpdate(Map<String, Object> extension) {
    }

    /**************************
     * Module proxy
     ***************************/
    @Override
    public boolean sendMessage(IMMessage msg) {
        ChatRoomMessage message = (ChatRoomMessage) msg;
        Map<String, Object> ext = new HashMap<>();
        ChatRoomMember chatRoomMember = ChatRoomMemberCache.getInstance().getChatRoomMember(roomId, NetIMCache.getAccount());
        if (chatRoomMember != null && chatRoomMember.getMemberType() != null) {
            ext.put("type", chatRoomMember.getMemberType().getValue());
            message.setRemoteExtension(ext);
        }
        NIMClient.getService(ChatRoomService.class).sendMessage(message, false).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                LogUtil.d(TAG, "发送消息成功");
            }

            @Override
            public void onFailed(int code) {
                if (code == ResponseCode.RES_CHATROOM_MUTED) {
                    Toast.makeText(NetIMCache.getContext(), "用户被禁言", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NetIMCache.getContext(), "消息发送失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onException(Throwable exception) {
                Toast.makeText(NetIMCache.getContext(), "消息发送失败！", Toast.LENGTH_SHORT).show();
            }
        });
        messageListPanel.onMsgSend(msg);
        return true;
    }

    @Override
    public void onInputPanelExpand() {
        controlContainer.setVisibility(View.GONE);
        if (fakeListText != null) {
            fakeListText.setVisibility(View.GONE);
        }
        if (isOnMic && roomInfo.getCreator().equals(NetIMCache.getAccount())) {
            connectionViewLayout.setVisibility(View.GONE);
            bypassVideoRender.setVisibility(View.GONE);
            bypassVideoRender.setZOrderMediaOverlay(false);
        }
    }

    @Override
    public void shouldCollapseInputPanel() {
        inputPanel.collapse(false);
        controlContainer.setVisibility(View.VISIBLE);
        if (fakeListText != null) {
            fakeListText.setVisibility(View.VISIBLE);
        }
        if (isOnMic && roomInfo.getCreator().equals(NetIMCache.getAccount())) {
            connectionViewLayout.setVisibility(View.VISIBLE);
            bypassVideoRender.setVisibility(View.VISIBLE);
            bypassVideoRender.setZOrderMediaOverlay(true);
        }
    }

    @Override
    public boolean isLongClickEnabled() {
        return false;
    }

    // 操作面板集合
    protected List<BaseAction> getActionList() {
        List<BaseAction> actions = new ArrayList<>();
        return actions;
    }

    /***********************
     * 连麦相关操作
     *****************************/

    View.OnClickListener baseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.interaction_close_btn) {
                connectionViewCloseBtn.setVisibility(View.GONE);
                connectionCloseConfirmLayout.setVisibility(View.VISIBLE);
                if (style == AVChatType.AUDIO.getValue()) {
                    connectionCloseConfirmTipsTv.setText(R.string.interaction_audio_close_title);
                } else {
                    connectionCloseConfirmTipsTv.setText(R.string.interaction_video_close_title);
                }
            } else if (i == R.id.close_confirm) {
                doCloseInteraction();
                doCloseInteractionView();
            } else if (i == R.id.close_cancel) {
                connectionCloseConfirmLayout.setVisibility(View.GONE);
                connectionViewCloseBtn.setVisibility(View.VISIBLE);
            } else if (i == R.id.input_btn) {
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (!isCreator) {
                            //整体禁言只针对观众
                            if (!mIsMute) {
                                startInputActivity();
                            } else {
                                Toast.makeText(NetIMCache.getContext(), "您已经被禁言！", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            startInputActivity();
                        }
                    }
                });
            } else if (i == R.id.share_btn) {
                if (pullUrl != null) {
                    ClipboardManager cm = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    // 将文本内容放到系统剪贴板里。
                    cm.setText(pullUrl);
                    EasyAlertDialogHelper.showOneButtonDiolag(LivePlayerBaseActivity.this, R.string.share_address_dialog_title, R.string.share_address_dialog_message, R.string.share_address_dialog_know, false, null);
                }
            }
        }
    };

    /**************************
     * 互动连麦入队/出队操作
     **************************/
    // 加入连麦队列，由主播端实现
    protected void joinQueue(CustomNotification customNotification, JSONObject json) {
    }

    // 退出连麦队列，由主播端实现
    protected void exitQueue(CustomNotification customNotification) {
    }

    // 主播选中某人连麦，由观众实现
    protected void onMicLinking(JSONObject jsonObject) {
    }

    // 观众由于重新进入房间，而拒绝连麦，由主播实现
    protected void rejectConnecting(String account) {
    }

    // 收到连麦成功消息，由观众端实现
    protected void onMicConnectedMsg(ChatRoomMessage message) {
    }

    // 收到取消连麦消息,由观众的实现
    protected void onMicDisConnectedMsg(String account) {
    }

    // 子类继承
    protected void showConnectionView(String account, String nick, int style) {
        isOnMic = true;
        updateOnMicName(nick);
    }

    // 设置连麦者昵称
    protected void updateOnMicName(String nick) {
        LogUtil.d(TAG, "updateOnMicName: " + nick);
        if (nick == null) {
            return;
        }
        onMicNick = nick;
        onMicNameText.setVisibility(View.VISIBLE);
        onMicNameText.setText(String.format("连麦者:%s", nick));
    }

    // 断开连麦,由子类实现
    protected abstract void doCloseInteraction();

    // 主播断开连麦者，由观众实现
    protected void onMicCanceling() {
    }

    protected void doCloseInteractionView() {
        loadingClosingText.setText(style == AVChatType.AUDIO.getValue() ? R.string.audio_closed : R.string.video_closed);
        audienceLoadingLayout.setVisibility(View.VISIBLE);
        loadingNameText.setText(!TextUtils.isEmpty(onMicNick) ? onMicNick : "");
        livingBg.setVisibility(View.GONE);
        connectionViewCloseBtn.setVisibility(View.GONE);
        connectionCloseConfirmLayout.setVisibility(View.GONE);
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                resetConnectionView();
            }
        }, 2000);
    }

    protected void resetConnectionView() {
        isOnMic = false;
        if (connectionViewLayout != null) {
            connectionViewLayout.setVisibility(View.GONE);
        }
        if (connectionCloseConfirmLayout != null) {
            connectionCloseConfirmLayout.setVisibility(View.GONE);
        }

        if (audienceLivingLayout != null) {
            audienceLivingLayout.setVisibility(View.GONE);
        }

        if(audioModeBypassLayout != null){
            audioModeBypassLayout.setVisibility(View.GONE);
        }

        if (audienceLoadingLayout != null) {
            audienceLoadingLayout.setVisibility(View.GONE);
        }
        if (connectionViewCloseBtn != null) {
            connectionViewCloseBtn.setVisibility(View.VISIBLE);
        }
        if (connectionViewCloseBtn != null) {
            onMicNameText.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * ***************************** 部分机型键盘弹出会造成布局挤压的解决方案 ***********************************
     */
    private InputConfig inputConfig = new InputConfig(false, false, false);

    private void startInputActivity() {
        InputActivity.startActivityForResult(this, messageEditText.getText().toString(), inputConfig, new InputActivity.InputActivityProxy() {
            @Override
            public void onSendMessage(String text) {
                inputPanel.onTextMessageSendButtonPressed(text);
            }
        });
        inputPanel.collapse(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == InputActivity.REQ_CODE) {
            // 设置EditText显示的内容
            String text = data.getStringExtra(InputActivity.EXTRA_TEXT);
            MoonUtil.identifyFaceExpression(NetIMCache.getContext(), messageEditText, text, ImageSpan.ALIGN_BOTTOM);
            messageEditText.setSelection(text.length());
            inputPanel.hideInputPanel();
            // 根据mode显示表情布局或者键盘布局
            int mode = data.getIntExtra(InputActivity.EXTRA_MODE, InputActivity.MODE_KEYBOARD_COLLAPSE);
            if (mode == InputActivity.MODE_SHOW_EMOJI) {
                inputPanel.toggleEmojiLayout();
            } else if (mode == InputActivity.MODE_SHOW_MORE_FUNC) {
                inputPanel.toggleActionPanelLayout();
            }
        }
    }

    //网络监听
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) return;
            //判断网络是否连接上 和 是否可用
            if (NetworkUtil.isNetAvailable(LivePlayerBaseActivity.this) && NetworkUtil.isNetworkConnected(LivePlayerBaseActivity.this)) {
                int networkType = NetworkUtil.getNetworkTypeForLink(LivePlayerBaseActivity.this);
                if (NetworkUtil.LinkNetWorkType._2G == networkType || NetworkUtil.LinkNetWorkType._3G == networkType || NetworkUtil.LinkNetWorkType._4G == networkType) {
                    showNetWorkDialog();
                } else {
                    dismissNetWorkDialog();
                }
            } else {
                dismissNetWorkDialog();
            }
        }
    };

    //注册网络变化监听
    private void registerNetTypeChangeObserver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    //注销网络监听
    private void unRegisterNetTypeChangeObserver() {
        unregisterReceiver(receiver);
    }

    private void showNetWorkDialog() {
        if (mNetworkDialog == null) {
            mNetworkDialog = new AlertDialog.Builder(this).setTitle("正在使用手机流量,是否继续播放").setNegativeButton("是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismissNetWorkDialog();
                }
            }).setPositiveButton("否", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismissNetWorkDialog();
                    onBackPressed();
                }
            }).create();
        }

        if (!mNetworkDialog.isShowing()) mNetworkDialog.show();
    }

    private void dismissNetWorkDialog() {
        if (mNetworkDialog != null) {
            mNetworkDialog.dismiss();
        }
    }

    private void showMemberDialog() {
    }


    //--------------------------------用户进出房间的动画-------------------------------------------
    //进的动画
    private TranslateAnimation mTranslateAnimationIn;
    //出的动画
    private TranslateAnimation mTranslateAnimationOut;

    //进的队列
    private List<ChatRoomNotificationAttachment> mQueueIn = new ArrayList<>();
    //出的队列
    private List<ChatRoomNotificationAttachment> mQueueOut = new ArrayList<>();

    //是否正在执行进的动画
    private boolean mIning = false;
    //是否正在执行出的动画
    private boolean mOuting = false;

    /**
     * 初始化动画
     */
    private void initAnim() {
        mTranslateAnimationIn = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1.2F, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        mTranslateAnimationIn.setDuration(1000);

        mTranslateAnimationOut = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1.2f, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        mTranslateAnimationOut.setDuration(1000);
    }

    /**
     * 进
     */
    @SuppressLint("SetTextI18n")
    private void userIntoRoomAnim() {
        if (mQueueIn.size() == 0 || mIning || isStop()) {
            return;
        }

        final ChatRoomNotificationAttachment attachment = mQueueIn.get(0);
        mIvIntoMemberHead.loadBuddyAvatar(attachment.getOperator());
        mTvIntoMemberName.setText(attachment.getOperatorNick() + "进入房间");
        mLlMemberInto.setVisibility(View.VISIBLE);
        mTranslateAnimationIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIning = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIning = false;
                if (mQueueIn.contains(attachment)) {
                    mQueueIn.remove(attachment);
                }
                if (mQueueIn.size() == 0) {
                    mLlMemberInto.setVisibility(View.GONE);
                } else {
                    userIntoRoomAnim();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mLlMemberInto.startAnimation(mTranslateAnimationIn);
    }

    /**
     * 出
     */
    @SuppressLint("SetTextI18n")
    private void userLeaveRoomAnim() {
        if (mQueueOut.size() == 0 || mOuting || isStop()) {
            return;
        }
        final ChatRoomNotificationAttachment attachment = mQueueOut.get(0);
        mIvLeaveMemberHead.loadBuddyAvatar(attachment.getOperator());
        mTvLeaveMemberName.setText(attachment.getOperatorNick() + "进入房间");
        mLlMemberLeave.setVisibility(View.VISIBLE);
        mTranslateAnimationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mOuting = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mOuting = false;
                if (mQueueOut.contains(attachment)) {
                    mQueueOut.remove(attachment);
                }
                if (mQueueOut.size() == 0) {
                    mLlMemberLeave.setVisibility(View.GONE);
                } else {
                    userLeaveRoomAnim();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mLlMemberLeave.startAnimation(mTranslateAnimationOut);
    }

    /**
     * 用户进入或者离开房间的动画
     *
     * @param name
     * @param in   true进入
     */
    private void userIntoOrLeaveRoomAnim(String account, String name, final boolean in) {
        final Handler handler = new Handler();
        final TranslateAnimation translateAnimation;
        if (in) {
            translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1.3F, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        } else {
            translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1.3F, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        }
        translateAnimation.setDuration(1000);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (in) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mLlMemberInto.setVisibility(View.GONE);
                        }
                    }, 1000);
                } else {
                    mLlMemberInto.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mIvIntoMemberHead.loadBuddyAvatar(account);
        mTvIntoMemberName.setText(String.format("%s%s房间", name, in ? "进入" : "离开"));
        mLlMemberInto.setVisibility(View.VISIBLE);

        if (in) {
            mLlMemberInto.startAnimation(translateAnimation);

        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLlMemberInto.startAnimation(translateAnimation);
                }
            }, 1000);
        }
    }

    protected boolean isStop() {
        return false;
    }
}

package com.netease.nim.yl.interactlive.entertainment.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.impl.cache.ChatRoomMemberCache;
import com.netease.nim.yl.NetIMCache;
import com.netease.nim.yl.R;
import com.netease.nim.yl.interactlive.entertainment.adapter.GiftAdapter;
import com.netease.nim.yl.interactlive.entertainment.adapter.InteractionAdapter;
import com.netease.nim.yl.interactlive.entertainment.constant.GiftConstant;
import com.netease.nim.yl.interactlive.entertainment.constant.GiftType;
import com.netease.nim.yl.interactlive.entertainment.constant.LiveType;
import com.netease.nim.yl.interactlive.entertainment.constant.MicStateEnum;
import com.netease.nim.yl.interactlive.entertainment.constant.PushLinkConstant;
import com.netease.nim.yl.interactlive.entertainment.helper.GiftCache;
import com.netease.nim.yl.interactlive.entertainment.helper.MicHelper;
import com.netease.nim.yl.interactlive.entertainment.http.ChatRoomHttpClient;
import com.netease.nim.yl.interactlive.entertainment.model.Gift;
import com.netease.nim.yl.interactlive.entertainment.model.InteractionMember;
import com.netease.nim.yl.interactlive.entertainment.widget.TargetPopWindow;
import com.netease.nim.yl.interactlive.im.file.AttachmentStore;
import com.netease.nim.yl.interactlive.permission.MPermission;
import com.netease.nim.yl.interactlive.permission.annotation.OnMPermissionDenied;
import com.netease.nim.yl.interactlive.permission.annotation.OnMPermissionGranted;
import com.netease.nim.yl.interactlive.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nim.yl.interactlive.permission.util.MPermissionUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.avchat.AVChatCallback;
import com.netease.nimlib.sdk.avchat.AVChatManager;
import com.netease.nimlib.sdk.avchat.constant.AVChatDeviceEvent;
import com.netease.nimlib.sdk.avchat.constant.AVChatMediaCodecMode;
import com.netease.nimlib.sdk.avchat.constant.AVChatNetworkQuality;
import com.netease.nimlib.sdk.avchat.constant.AVChatResCode;
import com.netease.nimlib.sdk.avchat.constant.AVChatType;
import com.netease.nimlib.sdk.avchat.constant.AVChatUserRole;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoCaptureOrientation;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoCropRatio;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoFrameRate;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoQuality;
import com.netease.nimlib.sdk.avchat.constant.AVChatVideoScalingType;
import com.netease.nimlib.sdk.avchat.model.AVChatAudioFrame;
import com.netease.nimlib.sdk.avchat.model.AVChatCameraCapturer;
import com.netease.nimlib.sdk.avchat.model.AVChatChannelInfo;
import com.netease.nimlib.sdk.avchat.model.AVChatLiveCompositingLayout;
import com.netease.nimlib.sdk.avchat.model.AVChatNetworkStats;
import com.netease.nimlib.sdk.avchat.model.AVChatParameters;
import com.netease.nimlib.sdk.avchat.model.AVChatSessionStats;
import com.netease.nimlib.sdk.avchat.model.AVChatSurfaceViewRenderer;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoCapturerFactory;
import com.netease.nimlib.sdk.avchat.model.AVChatVideoFrame;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomUpdateInfo;
import com.netease.nimlib.sdk.chatroom.model.EnterChatRoomData;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nrtc.sdk.common.ImageFormat;
import com.netease.vcloud.video.effect.VideoEffect;
import com.netease.vcloud.video.effect.VideoEffectFactory;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
 * 互动直播开发指南文档地址：
 * http://dev.netease.im/docs/product/互动直播/SDK开发集成/Android开发集成
 */
public class LiveActivity extends LivePlayerBaseActivity implements InteractionAdapter.MemberLinkListener {
    private static final String TAG = "LiveActivity";
    private final int USER_LEAVE_OVERTIME = 10 * 1000;
    private final int USER_JOIN_OVERTIME = 10 * 1000;
    // view
    private View backBtn;
    private View startLayout;
    private ImageView startLiveBgIv;
    private Button startBtn;
    private ImageButton switchBtn;
    private TextView noGiftText;
    private ViewGroup liveFinishLayout;
    private Button liveFinishBtn;
    private ImageButton controlExtendBtn;
    private LinearLayout controlBtnMid;
    private TextView hdBtn;
    private LinearLayout videoClarityLayout;
    private RelativeLayout videoClarityBlankView;
    private RelativeLayout rlVideoClarityHd;
    private ImageView ivVideoClarityHd;
    private RelativeLayout rlVideoClaritySd;
    private ImageView ivVideoClaritySd;
    private TextView btnVideoClarityCancel;
    private boolean isVideoClaritySd = false;
    private LinearLayout startLiveControlLayout;
    private ImageButton screenSwitchBtn;
    private ImageButton screenCameraBtn;
    private ImageButton screenBeautyBtn;
    private ImageButton bannedBtn;
    private FrameLayout targetBtn;
    private TextView targetTxt;
    private LinearLayout startLiveSwitchLayout;
    private LinearLayout screenSwitchHorizontal;
    private LinearLayout screenSwitchVertical;
    private View screenSwitchCover;
    private ViewGroup interactionLayout; // 互动布局
    private TextView noApplyText; // 暂无互动申请
    private TextView applyCountText;
    private GridView interactionGridView;
    private AVChatSurfaceViewRenderer videoRender; // 主播画面
    private ImageButton beautyBtn; // 美颜按钮
    private LinearLayout videoBeautyLayout;
    private LinearLayout videoBeautyContentView;
    private RelativeLayout videoBeautyBlankView;
    private LinearLayout videoBeautyOrigin;
    private LinearLayout videoBeautyNatural;
    private TextView videoBeautyCancel;
    private TextView videoBeautyConfirm;
    private LinearLayout videoBeautyStrength;
    private ImageView videoBeautyOriginIv;
    private ImageView videoBeautyNaturalIv;
    private SeekBar videoBeautyDipStrengthControlSb;
    private SeekBar videoBeautyContrastStrengthControlSb;
    private boolean isVideoBeautyOriginCurrent = false; //美颜默认打开
    private boolean isVideoBeautyOriginLast = false; //美颜默认打开
    private boolean isVideoFlashOpen = false; //闪光灯默认关闭
    /* 网络状态 */
    private ViewGroup networkStateLayout;
    private TextView netStateTipText; // 网络状态提示
    private ImageView netStateImage;
    //private TextView netOperateText; // 网络操作提示

    // state
    private boolean disconnected = false; // 是否断网（断网重连用）
    private boolean isStartLive = false; // 是否开始直播推流
    private boolean isWaiting = false; // 是否正在等待连麦成功
    private boolean isStartLiving = false; //是否正在开始直播
    private boolean isPermissionGrant = false;
    private boolean isDestroyRtc = false;
    private boolean isBeautyBtnCancel = false;
    // data
    private List<Gift> giftList = new ArrayList<>(); // 礼物列表数据
    private int interactionCount = 0; // 互动申请人数
    private InteractionAdapter interactionAdapter; // 互动人员adapter
    private List<InteractionMember> interactionDataSource; // 互动人员列表
    private String clickAccount; // 选择的互动人员帐号
    private InteractionMember currentInteractionMember; // 当前连麦者
    private InteractionMember nextInteractionMember; // 下一个选中连麦者

    private AVChatCameraCapturer mVideoCapturer;
    private Handler mVideoEffectHandler;
    private TargetPopWindow targetPopWindow;
    private String liveCid;
    private String targetType="a";

    public void setTargetType(String targetType) {
        this.targetType = targetType;
        chageTxt(targetType);
    }


    public static void start(Context context, boolean isVideo, boolean isCreator,
                             String roomId, String cid,String pushUrl, String pullUrl) {
        Intent intent = new Intent();
        intent.setClass(context, LiveActivity.class);
        intent.putExtra(EXTRA_MODE, isVideo);
        intent.putExtra(EXTRA_CREATOR, isCreator);
        intent.putExtra(EXTRA_LIVE_CID, cid);
        intent.putExtra(EXTRA_ROOM_ID, roomId);
        intent.putExtra(EXTRA_PUSH_URL, pushUrl);
        intent.putExtra(EXTRA_PULL_URL, pullUrl);

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        liveCid = getIntent().getStringExtra(EXTRA_LIVE_CID);
        findViews();
        updateRoomUI(true);
        loadGift();
        registerLiveObservers(true);

        //目前伴音功能的音乐文件，nrtc的SDK只支持读取存储空间里面的音乐文件，不支持assets中的文件，所以这里将文件拷贝到存储空间里面
        if (Environment.getExternalStorageDirectory() != null) {
            AttachmentStore.copy(this, "music/first_song.mp3", Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getPackageName() + "/music", "/first_song.mp3");
            AttachmentStore.copy(this, "music/second_song.mp3", Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getPackageName() + "/music", "/second_song.mp3");
        }
        if (disconnected) {
            // 如果网络不通
            Toast.makeText(LiveActivity.this, R.string.net_broken, Toast.LENGTH_SHORT).show();
            return;
        }
        startLiveSwitchLayout.setVisibility(View.GONE);
        requestLivePermission(); // 请求权限
    }

    @Override
    protected void setEnterRoomExtension(EnterChatRoomData enterChatRoomData) {
        Map<String, Object> notifyExt = new HashMap<>();
        if (liveType == LiveType.VIDEO_TYPE) {
            notifyExt.put(PushLinkConstant.type, AVChatType.VIDEO.getValue());
        } else if (liveType == LiveType.AUDIO_TYPE) {
            notifyExt.put(PushLinkConstant.type, AVChatType.AUDIO.getValue());
        }
        notifyExt.put(PushLinkConstant.meetingName, meetingName);
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        notifyExt.put(PushLinkConstant.orientation, isPortrait ? 1 : 2);
        enterChatRoomData.setNotifyExtension(notifyExt);
    }

    @Override
    protected int getActivityLayout() {
        return R.layout.live_player_activity;
    }

    @Override
    protected int getLayoutId() {
        return R.id.live_layout;
    }

    @Override
    protected int getControlLayout() {
        return liveType == LiveType.VIDEO_TYPE ? R.layout.live_video_control_layout : R.layout.live_audio_control_layout;
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        boolean isVideo = getIntent().getBooleanExtra(EXTRA_MODE, true);
        liveType = isVideo ? LiveType.VIDEO_TYPE : LiveType.AUDIO_TYPE;
    }

    @Override
    public void onBackPressed() {
        if (isStartLive) {
            logoutChatRoom();
        } else {
            releaseRtc(true, false);
            clearChatRoom();
        }
    }

    @Override
    protected void onDestroy() {
        giftList.clear();
        registerLiveObservers(false);
        super.onDestroy();
    }

    /**
     * 主播主动语音连麦
     */
    @Override
    protected void anchorLinkAudio(final ChatRoomMember chatRoomMember) {
        super.anchorLinkAudio(chatRoomMember);
        ChatRoomHttpClient.getInstance().pushMicLink(roomId, chatRoomMember.getAccount(), "语音", new ChatRoomHttpClient.ChatRoomHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                KLog.d(TAG, "doAudioLink onSuccess");
                InteractionMember member = new InteractionMember(chatRoomMember.getAccount(), chatRoomMember.getNick(), chatRoomMember.getAvatar(), AVChatType.AUDIO);
                member.setAvatarLink(true);
                onClick(member);
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                LogUtil.d(TAG, "http push mic link errorMsg:" + errorMsg);
            }
        });
    }

    /**
     * 主播主动视频连麦
     */
    @Override
    protected void anchorLinkVideo(final ChatRoomMember chatRoomMember) {
        super.anchorLinkVideo(chatRoomMember);
        ChatRoomHttpClient.getInstance().pushMicLink(roomId, NetIMCache.getAccount(), "视频", new ChatRoomHttpClient.ChatRoomHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                InteractionMember member = new InteractionMember(chatRoomMember.getAccount(), chatRoomMember.getNick(), chatRoomMember.getAvatar(), AVChatType.VIDEO);
                member.setAvatarLink(true);
                onClick(member);
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                LogUtil.d(TAG, "join queue failed, code:" + code);
            }
        });
    }

    /**
     * 退出聊天室
     */
    private void logoutChatRoom() {
        if (startLayout.getVisibility() == View.VISIBLE) {
            doCompletelyFinish();
        } else {
            EasyAlertDialogHelper.createOkCancelDiolag(this, null, getString(R.string.finish_confirm),
                    getString(R.string.confirm), getString(R.string.cancel), true,
                    new EasyAlertDialogHelper.OnDialogActionListener() {
                        @Override
                        public void doCancelAction() {

                        }

                        @Override
                        public void doOkAction() {
                            doCompletelyFinish();
                        }
                    }).show();
        }
    }

    private void doCompletelyFinish() {
        isStartLive = false;
        doUpdateRoomInfo();
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                releaseRtc(true, true);
                finish();
            }
        }, 50);
    }

    private void showLiveFinishLayout() {
        liveFinishLayout.setVisibility(View.VISIBLE);
        TextView masterNickText = findView(R.id.finish_master_name);
        masterNickText.setText(TextUtils.isEmpty(masterNick) ? (roomInfo == null ? "" : roomInfo.getCreator()) : masterNick);
    }


    private void doUpdateRoomInfo() {
        ChatRoomUpdateInfo chatRoomUpdateInfo = new ChatRoomUpdateInfo();
        Map<String, Object> map = new HashMap<>(1);
        map.put(PushLinkConstant.type, -1);
        chatRoomUpdateInfo.setExtension(map);
        NIMClient.getService(ChatRoomService.class)
                .updateRoomInfo(roomId, chatRoomUpdateInfo, true, map)
                .setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        LogUtil.i(TAG, "leave room, update room info success");
                    }

                    @Override
                    public void onFailed(int i) {
                        LogUtil.e(TAG, "leave room, update room info failed, code:" + i);
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        LogUtil.e(TAG, "leave room, update room info onException, throwable:" + throwable.getMessage());
                    }
                });
    }

    // 清空聊天室缓存
    private void clearChatRoom() {
        ChatRoomMemberCache.getInstance().clearRoomCache(roomId);
        finish();
    }


    /***********************
     * join channel
     ***********************/
    protected void joinChannel(String pushUrl) {
        if (isDestroyed || isDestroyRtc) {
            return;
        }
        //设置服务器录制参数
        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_SERVER_LIVE_RECORD, true);
        AVChatManager.getInstance().setParameter(AVChatParameters.KEY_SESSION_LIVE_URL, pushUrl);
        MicHelper.getInstance().joinChannel(meetingName, liveType == LiveType.VIDEO_TYPE, new MicHelper.ChannelCallback() {

            @Override
            public void onJoinChannelSuccess() {
                if (liveType == LiveType.AUDIO_TYPE) {
                    AVChatManager.getInstance().setSpeaker(true);
                }
                MicHelper.getInstance().sendBrokeMicMsg(roomId, null);
                dropQueue();
            }

            @Override
            public void onJoinChannelFailed() {
                showLiveFinishLayout();
            }
        });
    }

    /*****************************
     * 初始化
     *****************************/

    protected void findViews() {
        super.findViews();
        rootView = findView(R.id.live_layout);
        videoRender = findView(R.id.video_render);
        videoRender.setZOrderMediaOverlay(false);
        backBtn = findView(R.id.BackBtn);
        startLayout = findViewById(R.id.start_layout);
        startLiveBgIv = findViewById(R.id.start_live_bg_iv);
        startBtn = findViewById(R.id.start_live_btn);
        switchBtn = findViewById(R.id.switch_btn);
        bannedBtn =  findViewById(R.id.banned_btn);
        noGiftText = findView(R.id.no_gift_tip);
        interactionLayout = findView(R.id.live_interaction_layout);
        noApplyText = findView(R.id.no_apply_tip);
        applyCountText = findView(R.id.apply_count_text);
        fakeListText = findView(R.id.fake_list_text);
        if (liveType == LiveType.VIDEO_TYPE && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            fakeListText.setVisibility(View.VISIBLE);
        } else {
            fakeListText.setVisibility(View.GONE);
        }
        //底部控制按钮
        controlExtendBtn = findView(R.id.control_extend_btn);
        controlBtnMid = findView(R.id.control_btn_mid);

        // 高清
        hdBtn = findView(R.id.hd_btn);
        findClarityLayout();
        //开始直播页面按钮
        startLiveControlLayout = findView(R.id.start_live_control_layout);
        screenSwitchBtn = findView(R.id.start_screen_btn);
        screenCameraBtn = findView(R.id.start_switch_btn);
        screenBeautyBtn = findView(R.id.start_beauty_btn);
        targetBtn= findView(R.id.target_btn);
        targetTxt=findView(R.id.target_txt);
        startLiveSwitchLayout = findView(R.id.live_screen_switch_layout);
        screenSwitchHorizontal = findView(R.id.screen_switch_horizontal);
        screenSwitchVertical = findView(R.id.screen_switch_vertical);
        screenSwitchCover = findView(R.id.live_screen_switch_cover);

        //美颜
        beautyBtn = findView(R.id.beauty_btn);
        findBeautyLayout();

        // 直播结束
        liveFinishLayout = findView(R.id.live_finish_layout);
        liveFinishBtn = findView(R.id.finish_btn);
        liveFinishBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NIMClient.getService(ChatRoomService.class).exitChatRoom(roomId);
                releaseRtc(false, false);
                clearChatRoom();
            }
        });
        // 初始化连线人员布局
        findInteractionMemberLayout();
        setListener();
        // 视频/音频，布局设置
        if (liveType == LiveType.AUDIO_TYPE) {
            switchBtn.setVisibility(View.GONE);
            hdBtn.setVisibility(View.GONE);
            beautyBtn.setVisibility(View.GONE);
            startLiveControlLayout.setVisibility(View.GONE);
        } else if (liveType == LiveType.VIDEO_TYPE) {
            switchBtn.setVisibility(View.VISIBLE);
            hdBtn.setVisibility(View.VISIBLE);
            beautyBtn.setVisibility(View.VISIBLE);
            startLiveControlLayout.setVisibility(View.VISIBLE);
        }
        // 网络状态
        networkStateLayout = findView(R.id.network_state_layout);
        netStateTipText = findView(R.id.net_state_tip);
        netStateImage = findView(R.id.network_image);
    }

    private void findBeautyLayout() {
        videoBeautyLayout = findView(R.id.video_beauty_layout);
        videoBeautyContentView = findView(R.id.background_beauty_content_view);
        videoBeautyBlankView = findView(R.id.video_beauty_blank_view);
        videoBeautyOrigin = findView(R.id.video_beauty_origin);
        videoBeautyNatural = findView(R.id.video_beauty_natural);
        videoBeautyCancel = findView(R.id.video_beauty_button_cancel);
        videoBeautyConfirm = findView(R.id.video_beauty_button_confirm);
        videoBeautyStrength = findView(R.id.beauty_strength);
        videoBeautyOriginIv = findView(R.id.video_beauty_origin_iv);
        videoBeautyNaturalIv = findView(R.id.video_beauty_natural_iv);
        videoBeautyDipStrengthControlSb = findView(R.id.beauty_dip_strength_control);
        videoBeautyContrastStrengthControlSb = findView(R.id.beauty_contrast_strength_control);

        videoBeautyBlankView.setOnClickListener(beautyListener);
        videoBeautyContentView.setOnClickListener(beautyListener);
        videoBeautyOrigin.setOnClickListener(beautyListener);
        videoBeautyNatural.setOnClickListener(beautyListener);
        videoBeautyCancel.setOnClickListener(beautyListener);
        videoBeautyConfirm.setOnClickListener(beautyListener);

        videoBeautyDipStrengthControlSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mVideoEffect == null) {
                    return;
                }
                mVideoEffect.setBeautyLevel(seekBar.getProgress() / 20);
            }
        });
        videoBeautyContrastStrengthControlSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mVideoEffect == null) {
                    return;
                }
                mVideoEffect.setFilterLevel((float) seekBar.getProgress() / 100);
            }
        });
    }

    private void findClarityLayout() {
        videoClarityLayout = findView(R.id.video_clarity_layout);
        videoClarityBlankView = findView(R.id.video_clarity_blank_view);
        rlVideoClarityHd = findView(R.id.video_clarity_hd_rl);
        ivVideoClarityHd = findView(R.id.video_clarity_hd_iv);
        rlVideoClaritySd = findView(R.id.video_clarity_sd_rl);
        ivVideoClaritySd = findView(R.id.video_clarity_sd_iv);
        btnVideoClarityCancel = findView(R.id.video_clarity_button_cancel);

        videoClarityBlankView.setOnClickListener(clarityListener);
        rlVideoClarityHd.setOnClickListener(clarityListener);
        rlVideoClaritySd.setOnClickListener(clarityListener);
        btnVideoClarityCancel.setOnClickListener(clarityListener);
    }

    // 初始化礼物布局
    protected void findGiftLayout() {
        super.findGiftLayout();
        adapter = new GiftAdapter(giftList, this);
        giftView.setAdapter(adapter);
    }

    private OnClickListener beautyListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.video_beauty_natural) {
                isVideoBeautyOriginCurrent = false;
                updateBeautyLayout(isVideoBeautyOriginCurrent);
            } else if (i == R.id.video_beauty_origin) {
                isVideoBeautyOriginCurrent = true;
                updateBeautyLayout(isVideoBeautyOriginCurrent);
            } else if (i == R.id.video_beauty_button_cancel) {
                videoBeautyLayout.setVisibility(View.GONE);
                isBeautyBtnCancel = true;
                updateBeautyLayout(isVideoBeautyOriginLast);
                updateBeautyIcon(isVideoBeautyOriginLast);
            } else if (i == R.id.video_beauty_button_confirm) {
                isBeautyBtnCancel = false;
                isVideoBeautyOriginLast = isVideoBeautyOriginCurrent;
                updateBeautyIcon(isVideoBeautyOriginLast);
                videoBeautyLayout.setVisibility(View.GONE);
            } else if (i == R.id.video_beauty_blank_view) {
                videoBeautyLayout.setVisibility(View.GONE);
                isBeautyBtnCancel = true;
                updateBeautyLayout(isVideoBeautyOriginLast);
                updateBeautyIcon(isVideoBeautyOriginLast);
            }
        }
    };


    /**
     * 更新美颜按钮显示
     */
    private void updateBeautyLayout(boolean isBeautyClose) {
        videoBeautyOriginIv.setSelected(isBeautyClose);
        videoBeautyNaturalIv.setSelected(!isBeautyClose);
        videoBeautyStrength.setVisibility(isBeautyClose ? View.GONE : View.VISIBLE);
    }

    private void updateBeautyIcon(boolean isBeautyClose) {
        if (isBeautyClose) {
            screenBeautyBtn.setBackgroundResource(R.drawable.ic_beauty_close_selector);
            beautyBtn.setBackgroundResource(R.drawable.ic_beauty_close_selector);
        } else {
            screenBeautyBtn.setBackgroundResource(R.drawable.ic_beauty_open_selector);
            beautyBtn.setBackgroundResource(R.drawable.ic_beauty_open_selector);
        }
    }

    private OnClickListener clarityListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.video_clarity_hd_rl) {
                isVideoClaritySd = false;
                ivVideoClarityHd.setVisibility(View.VISIBLE);
                ivVideoClaritySd.setVisibility(View.GONE);
                setVideoQuality(AVChatVideoQuality.QUALITY_720P);
                hdBtn.setText("高清");
                closeClarityLayout(true);
            } else if (i == R.id.video_clarity_sd_rl) {
                isVideoClaritySd = true;
                ivVideoClarityHd.setVisibility(View.GONE);
                ivVideoClaritySd.setVisibility(View.VISIBLE);
                setVideoQuality(AVChatVideoQuality.QUALITY_480P);
                hdBtn.setText("普清");
                closeClarityLayout(true);
            } else if (i == R.id.video_clarity_button_cancel) {
                closeClarityLayout(false);
            } else if (i == R.id.video_clarity_blank_view) {
                closeClarityLayout(false);
            }
        }
    };

    private void closeClarityLayout(boolean isDelayed) {
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                videoClarityLayout.setVisibility(View.GONE);
            }
        }, isDelayed ? 500 : 0);
    }

    private void findInteractionMemberLayout() {
        interactionGridView = findView(R.id.apply_grid_view);
        interactionDataSource = new ArrayList<>();
        interactionAdapter = new InteractionAdapter(interactionDataSource, this, this);
        interactionGridView.setAdapter(interactionAdapter);
        interactionGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InteractionMember member = (InteractionMember) interactionAdapter.getItem(position);
                member.setSelected(true);
                if (clickAccount != null && !clickAccount.equals(member.getAccount())) {
                    for (InteractionMember m : interactionDataSource) {
                        if (m.getAccount().equals(clickAccount)) {
                            m.setSelected(false);
                            break;
                        }
                    }
                }
                clickAccount = member.getAccount();
                interactionAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void updateUI() {
        super.updateUI();
        startLayout.setVisibility(View.GONE);
        ChatRoomMember roomMember = ChatRoomMemberCache.getInstance().getChatRoomMember(roomId, roomInfo.getCreator());
        if (roomMember != null) {
            masterNick = roomMember.getNick();
        }
        masterNameText.setText(TextUtils.isEmpty(masterNick) ? roomInfo.getCreator() : masterNick);
    }

    // 主播进来清空队列
    private void dropQueue() {
        NIMClient.getService(ChatRoomService.class).dropQueue(roomId).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "drop queue success");
            }

            @Override
            public void onFailed(int i) {
                LogUtil.d(TAG, "drop queue failed, code:" + i);
            }

            @Override
            public void onException(Throwable throwable) {
            }
        });
    }

    /**
     * 取出缓存的礼物
     */
    private void loadGift() {
        Map gifts = GiftCache.getInstance().getGift(roomId);
        if (gifts == null) {
            return;
        }
        for (Map.Entry<Integer, Integer> entry : (Iterable<Map.Entry<Integer, Integer>>) gifts.entrySet()) {
            int type = entry.getKey();
            int count = entry.getValue();
            giftList.add(new Gift(GiftType.typeOfValue(type), GiftConstant.titles[type], count, GiftConstant.images[type]));
        }
    }

    private void setListener() {
        screenSwitchBtn.setOnClickListener(buttonClickListener);
        screenCameraBtn.setOnClickListener(buttonClickListener);
        screenBeautyBtn.setOnClickListener(buttonClickListener);
        targetBtn.setOnClickListener(buttonClickListener);
        startBtn.setOnClickListener(buttonClickListener);
        screenSwitchHorizontal.setOnClickListener(buttonClickListener);
        screenSwitchVertical.setOnClickListener(buttonClickListener);
        screenSwitchCover.setOnClickListener(buttonClickListener);
        backBtn.setOnClickListener(buttonClickListener);
        controlExtendBtn.setOnClickListener(buttonClickListener);
        switchBtn.setOnClickListener(buttonClickListener);
        beautyBtn.setOnClickListener(buttonClickListener);
        interactionBtn.setOnClickListener(buttonClickListener);
        interactionLayout.setOnClickListener(buttonClickListener);
        giftBtn.setOnClickListener(buttonClickListener);
        giftLayout.setOnClickListener(buttonClickListener);
        bannedBtn.setOnClickListener(buttonClickListener);
        if (liveType == LiveType.VIDEO_TYPE) {
            hdBtn.setOnClickListener(buttonClickListener);
        }
    }

    private void registerLiveObservers(boolean register) {
        AVChatManager.getInstance().observeAVChatState(this, register);
    }

    OnClickListener buttonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.start_live_btn) {
                if (disconnected) {
                    // 如果网络不通
                    Toast.makeText(LiveActivity.this, R.string.net_broken, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (AVChatManager.checkPermission(LiveActivity.this).size() != 0) {
                    Toast.makeText(LiveActivity.this, R.string.permission_is_not_available, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isPermissionGrant) {
                    startPreview();
                }
                if (isStartLiving) {
                    return;
                }
                isStartLiving = true;
                startBtn.setText(R.string.live_prepare);
                startLiveSwitchLayout.setVisibility(View.GONE);
                chooseTarget();

            }else if(i == R.id.start_screen_btn){
                startLiveSwitchLayout.setVisibility(startLiveSwitchLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                if (startLiveSwitchLayout.getVisibility() == View.VISIBLE) {
                    updateLiveSwitchLayout(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
                }
            } if (i == R.id.start_switch_btn) {
                mVideoCapturer.switchCamera();
            } else if (i == R.id.start_beauty_btn) {
                if (Build.VERSION.SDK_INT < 18) {
                    Toast.makeText(LiveActivity.this, "需要4.3以上的Android版本才能使用该功能", Toast.LENGTH_SHORT).show();
                    return;
                }
                showBeautyLayout();
            } else if (i == R.id.screen_switch_horizontal) {
                updateLiveSwitchLayout(false);
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startLiveSwitchLayout.setVisibility(View.GONE);
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                }, 300);
            } else if (i == R.id.screen_switch_vertical) {
                updateLiveSwitchLayout(true);
                getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startLiveSwitchLayout.setVisibility(View.GONE);
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                }, 300);
            } else if (i == R.id.live_screen_switch_cover) {
                startLiveSwitchLayout.setVisibility(View.GONE);
            } else if (i == R.id.BackBtn) {
                if (isStartLive) {
                    logoutChatRoom();
                } else {
                    releaseRtc(true, false);
                    clearChatRoom();
                }
            } else if (i == R.id.control_extend_btn) {
                updateControlUI();
            } else if (i == R.id.switch_btn) {
                mVideoCapturer.switchCamera();
                mVideoCapturer.setZoom(0);
                if (isVideoFlashOpen) {
                    updateFlashIcon();
                }
            } else if (i == R.id.beauty_btn) {
                if (Build.VERSION.SDK_INT < 18) {
                    Toast.makeText(LiveActivity.this, "需要4.3以上的Android版本才能使用该功能", Toast.LENGTH_SHORT).show();
                    return;
                }
                showBeautyLayout();
            } else if (i == R.id.interaction_btn) {
                showInteractionLayout();
            } else if (i == R.id.live_interaction_layout) {
                interactionLayout.setVisibility(View.GONE);
            } else if (i == R.id.gift_btn) {
                showGiftLayout();
            } else if (i == R.id.gift_layout) {
                giftLayout.setVisibility(View.GONE);
            } else if (i == R.id.hd_btn) {
                showClarityLayout();
            }else if (i == R.id.banned_btn){
                muteAllChatRoom();
            }else if (i == R.id.target_btn){
                if (targetPopWindow == null) {
                    targetPopWindow = new TargetPopWindow(LiveActivity.this);
                }
                targetPopWindow.show(targetBtn);
            }
        }
    };

    /**
     * 对互动直播 聊天室 整体 禁言或者解禁
     */
    private void muteAllChatRoom(){
        NIMClient.getService(ChatRoomService.class).fetchRoomInfo(roomId).setCallback(new RequestCallback<ChatRoomInfo>() {
            @Override
            public void onSuccess(final ChatRoomInfo param) {
                String roomId = param.getRoomId();
                boolean isMute = param.isMute();
                muteChatRoomHttp(roomId,isMute);
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

    /**
     * 禁言 聊天室
     * @param roomId
     * @param isMute
     */
    private void muteChatRoomHttp(String roomId,final boolean isMute){
        String type;
        if (isMute){
            type = "off";
        }else {
            type = "on";
        }
        ChatRoomHttpClient.getInstance().muteLive(roomId,type,new ChatRoomHttpClient.ChatRoomHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "muteChatRoomHttp,onSuccess");
                if(isMute){
                    Toast.makeText(LiveActivity.this, "聊天室整体取消禁言成功！", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(LiveActivity.this, "聊天室整体设置禁言成功！", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                LogUtil.d(TAG, "muteChatRoomHttp,onFailed");
            }
        });
    }

    private void updateFlashIcon() {
        isVideoFlashOpen = !isVideoFlashOpen;
    }

    private void showBeautyLayout() {
        videoBeautyLayout.setVisibility(View.VISIBLE);
        updateBeautyLayout(isVideoBeautyOriginLast);
    }

    private void showClarityLayout() {
        inputPanel.collapse(true);
        videoClarityLayout.setVisibility(View.VISIBLE);
        ivVideoClarityHd.setVisibility(isVideoClaritySd ? View.GONE : View.VISIBLE);
        ivVideoClaritySd.setVisibility(isVideoClaritySd ? View.VISIBLE : View.GONE);
    }

    private void updateControlUI() {
        final boolean isHide = controlBtnMid.getVisibility() == View.VISIBLE;
        final TranslateAnimation translateAnimationTop;
        final TranslateAnimation translateAnimationMid;
        if (isHide) {
            translateAnimationTop = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 2);
            translateAnimationMid = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);
        } else {
            translateAnimationTop = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 2, Animation.RELATIVE_TO_SELF, 0);
            translateAnimationMid = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                    Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
        }
        translateAnimationTop.setDuration(500);
        translateAnimationMid.setDuration(500);

        translateAnimationMid.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (isHide) {
                    controlExtendBtn.setBackgroundResource(R.drawable.control_extend_top_selector);
                } else {
                    controlExtendBtn.setBackgroundResource(R.drawable.control_extend_bottom_selector);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isHide) {
                    controlBtnMid.setVisibility(View.INVISIBLE);
                } else {
                    controlBtnMid.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        controlBtnMid.post(new Runnable() {
            @Override
            public void run() {
                controlBtnMid.setVisibility(View.VISIBLE);
                controlBtnMid.startAnimation(translateAnimationMid);
            }
        });
    }

    private void chooseTarget() {
        ChatRoomHttpClient.getInstance().chooseTargetType(liveCid,targetType,new ChatRoomHttpClient.ChatRoomHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "chooseTargetHttp,onSuccess");
                createChannel();
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                LogUtil.d(TAG, "chooseTargetHttp,onFailed");
            }
        });




    }

    private void createChannel() {
        this.meetingName = StringUtil.get36UUID();
        // 这里用uuid，作为多人通话房间的名称
        AVChatManager.getInstance().createRoom(meetingName, null, new AVChatCallback<AVChatChannelInfo>() {
            @Override
            public void onSuccess(AVChatChannelInfo avChatChannelInfo) {
                pushMeetingName();
            }

            @Override
            public void onFailed(int i) {
                if (i == ResponseCode.RES_EEXIST) {
                    // 417表示该频道已经存在
                    LogUtil.e(TAG, "create room 417, enter room");
                    Toast.makeText(LiveActivity.this, "创建的房间名：" + meetingName, Toast.LENGTH_SHORT).show();
                    isStartLive = true;
                } else {
                    isStartLiving = false;
                    startBtn.setText(R.string.live_start);
                    LogUtil.e(TAG, "create room failed, code:" + i);
                }
            }

            @Override
            public void onException(Throwable throwable) {
                isStartLiving = false;
                startBtn.setText(R.string.live_start);
                LogUtil.e(TAG, "create room onException, throwable:" + throwable.getMessage());
            }
        });
    }

    public void pushMeetingName() {
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        ChatRoomHttpClient.getInstance().updateRoomInfo(roomId, meetingName,liveCid,isPortrait ? 1 : 2, new ChatRoomHttpClient.ChatRoomHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                LogUtil.d(TAG, "updateRoomInfo,onSuccess");
                isStartLive = true;
                findInputViews();
                pullUrl = getIntent().getStringExtra(EXTRA_PULL_URL);
                String pushUrl = getIntent().getStringExtra(EXTRA_PUSH_URL);
                joinChannel(pushUrl);
                enterRoom();
                startLayout.setVisibility(View.GONE);
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                LogUtil.d(TAG, "updateRoomInfo,onFailed");
            }
        });
    }

    /**
     * 主播直播前预览
     */
    private void startPreview() {
        AVChatManager.getInstance().enableRtc();
        if (liveType == LiveType.VIDEO_TYPE) {
            AVChatManager.getInstance().enableVideo();
        }
        if (mVideoCapturer == null) {
            mVideoCapturer = AVChatVideoCapturerFactory.createCameraCapturer();
            AVChatManager.getInstance().setupVideoCapturer(mVideoCapturer);
        }
        if (liveType == LiveType.VIDEO_TYPE) {
            AVChatManager.getInstance().setupLocalVideoRender(videoRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        }
        AVChatParameters parameters = new AVChatParameters();
        parameters.setBoolean(AVChatParameters.KEY_SESSION_LIVE_MODE, true);
        parameters.setInteger(AVChatParameters.KEY_SESSION_MULTI_MODE_USER_ROLE, AVChatUserRole.NORMAL);
        parameters.setString(AVChatParameters.KEY_VIDEO_ENCODER_MODE, AVChatMediaCodecMode.MEDIA_CODEC_SOFTWARE);
        parameters.setInteger(AVChatParameters.KEY_VIDEO_QUALITY, AVChatVideoQuality.QUALITY_720P);
        //如果用到美颜功能，建议这里设为15帧
        parameters.setInteger(AVChatParameters.KEY_VIDEO_FRAME_RATE, AVChatVideoFrameRate.FRAME_RATE_15);
        //如果不用美颜功能，这里可以设为25帧
        //parameters.setInteger(AVChatParameters.KEY_VIDEO_FRAME_RATE, AVChatVideoFrameRate.FRAME_RATE_25);
        parameters.set(AVChatParameters.KEY_SESSION_LIVE_COMPOSITING_LAYOUT, new AVChatLiveCompositingLayout(AVChatLiveCompositingLayout.Mode.LAYOUT_FLOATING_RIGHT_VERTICAL));
        parameters.setInteger(AVChatParameters.KEY_VIDEO_FIXED_CROP_RATIO, AVChatVideoCropRatio.CROP_RATIO_16_9);
        parameters.setBoolean(AVChatParameters.KEY_VIDEO_ROTATE_IN_RENDING, true);
        int videoOrientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? AVChatVideoCaptureOrientation.ORIENTATION_PORTRAIT : AVChatVideoCaptureOrientation.ORIENTATION_LANDSCAPE_RIGHT;
        parameters.setInteger(AVChatParameters.KEY_VIDEO_CAPTURE_ORIENTATION, videoOrientation);
        //设置美颜
        parameters.setBoolean(AVChatParameters.KEY_VIDEO_FRAME_FILTER, false);

        AVChatManager.getInstance().setParameters(parameters);
        if (liveType == LiveType.VIDEO_TYPE) {
            AVChatManager.getInstance().startVideoPreview();
            if (mVideoCapturer != null) {
                // mVideoCapturer.setAutoFocus(true);
            }
        }
    }

    private void updateLiveSwitchLayout(boolean isPortrait) {
        if (!isPortrait) {
            screenSwitchHorizontal.setSelected(true);
            screenSwitchVertical.setSelected(false);
        } else {
            screenSwitchHorizontal.setSelected(false);
            screenSwitchVertical.setSelected(true);
        }
    }

    // set video quality
    private void setVideoQuality(int quality) {
        AVChatParameters parameters = new AVChatParameters();
        parameters.setInteger(AVChatParameters.KEY_VIDEO_QUALITY, quality);
        AVChatManager.getInstance().setParameters(parameters);
    }

    // 显示礼物布局
    private void showGiftLayout() {
        inputPanel.collapse(true);// 收起软键盘
        giftLayout.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
        if (adapter.getCount() == 0) {
            // 暂无礼物
            noGiftText.setVisibility(View.VISIBLE);
        } else {
            noGiftText.setVisibility(View.GONE);
        }
    }

    protected void updateGiftList(GiftType type) {
        if (!updateGiftCount(type)) {
            giftList.add(new Gift(type, GiftConstant.titles[type.getValue()], 1, GiftConstant.images[type.getValue()]));
        }
        GiftCache.getInstance().saveGift(roomId, type.getValue());
    }

    // 更新收到礼物的数量
    private boolean updateGiftCount(GiftType type) {
        for (Gift gift : giftList) {
            if (type == gift.getGiftType()) {
                gift.setCount(gift.getCount() + 1);
                return true;
            }
        }
        return false;
    }

    private void releaseRtc(boolean isReleaseRtc, boolean isLeaveRoom) {
        if (liveType == LiveType.VIDEO_TYPE) {
            // 释放资源
            if (mVideoEffect != null) {
                LogUtil.d(TAG, "releaseRtc");
                isUninitVideoEffect = true;
                mHasSetFilterType = false;
                mVideoEffectHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.d(TAG, "releaseRtc unInit");
                        mVideoEffect.unInit();
                        mVideoEffect = null;
                    }
                });
            }
        }

        if (isReleaseRtc) {
            isDestroyRtc = true;
            MicHelper.getInstance().leaveChannel(liveType == LiveType.VIDEO_TYPE, liveType == LiveType.VIDEO_TYPE, isLeaveRoom, meetingName);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isUninitVideoEffect = false;
        findViews();
        updateRoomUI(true);
        loadGift();
        updateBeautyIcon(isVideoBeautyOriginLast);
        if (liveType == LiveType.VIDEO_TYPE && AVChatManager.checkPermission(this).size() == 0) {
            startLiveBgIv.setVisibility(View.GONE);
            AVChatParameters parameters = new AVChatParameters();
            int videoOrientation = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? AVChatVideoCaptureOrientation.ORIENTATION_PORTRAIT : AVChatVideoCaptureOrientation.ORIENTATION_LANDSCAPE_RIGHT;
            parameters.setInteger(AVChatParameters.KEY_VIDEO_CAPTURE_ORIENTATION, videoOrientation);
            AVChatManager.getInstance().setParameters(parameters);
            AVChatManager.getInstance().setupLocalVideoRender(videoRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        }
        if (AVChatManager.checkPermission(this).size() != 0 && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            startLiveBgIv.setBackgroundResource(R.drawable.live_start_landscape_bg);
        }
    }

    /**
     * ********************************** 断网重连处理 **********************************
     */

    // 网络连接成功
    protected void onConnected() {
        if (!disconnected) {
            return;
        }
        changeNetWorkTip(true);
        LogUtil.i(TAG, "live on connected");
        disconnected = false;
    }

    // 网络断开
    protected void onDisconnected() {
        LogUtil.i(TAG, "live on disconnected");
        disconnected = true;
        changeNetWorkTip(false);
    }

    private void changeNetWorkTip(boolean isShow) {
        if (networkStateLayout == null) {
            networkStateLayout = findView(R.id.network_state_layout);
        }
        networkStateLayout.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }


    /***********************
     * 录音摄像头权限申请
     *******************************/

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnMPermissionGranted(LIVE_PERMISSION_REQUEST_CODE)
    public void onLivePermissionGranted() {
        if (liveType == LiveType.VIDEO_TYPE) {
            startLiveBgIv.setVisibility(View.GONE);
        }
        isPermissionGrant = true;
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startPreview();
            }
        }, 50);
    }

    @OnMPermissionDenied(LIVE_PERMISSION_REQUEST_CODE)
    public void onLivePermissionDenied() {
        List<String> deniedPermissions = MPermission.getDeniedPermissions(this, LIVE_PERMISSIONS);
        String tip = "您拒绝了权限" + MPermissionUtil.toString(deniedPermissions) + "，无法开启在线课堂";
        Toast.makeText(LiveActivity.this, tip, Toast.LENGTH_SHORT).show();
    }

    @OnMPermissionNeverAskAgain(LIVE_PERMISSION_REQUEST_CODE)
    public void onLivePermissionDeniedAsNeverAskAgain() {
        List<String> deniedPermissions = MPermission.getDeniedPermissionsWithoutNeverAskAgain(this, LIVE_PERMISSIONS);
        List<String> neverAskAgainPermission = MPermission.getNeverAskAgainPermissions(this, LIVE_PERMISSIONS);
        StringBuilder sb = new StringBuilder();
        sb.append("无法开启在线课堂，请到系统设置页面开启权限");
        sb.append(MPermissionUtil.toString(neverAskAgainPermission));
        if (deniedPermissions != null && !deniedPermissions.isEmpty()) {
            sb.append(",下次询问请授予权限");
            sb.append(MPermissionUtil.toString(deniedPermissions));
        }
        Toast.makeText(LiveActivity.this, sb.toString(), Toast.LENGTH_LONG).show();
    }


    /*********************** 连麦申请/取消处理 *******************/

    // 主播让观众下麦的超时
    Runnable userLeaveRunnable = new Runnable() {
        @Override
        public void run() {
            isWaiting = false;
            Toast.makeText(LiveActivity.this, "超时，请重新连麦", Toast.LENGTH_SHORT).show();
            if (currentInteractionMember != null){
                currentInteractionMember.setMicStateEnum(MicStateEnum.NONE);
            }
            updateMemberListUI(nextInteractionMember, MicStateEnum.NONE);
        }
    };

    // 主播选择观众连麦的超时
    Runnable userJoinRunnable = new Runnable() {
        @Override
        public void run() {
            isWaiting = false;
            Toast.makeText(LiveActivity.this, "连麦超时", Toast.LENGTH_SHORT).show();
            if (currentInteractionMember != null) {
                currentInteractionMember.setMicStateEnum(MicStateEnum.NONE);
            }
            interactionAdapter.notifyDataSetChanged();
        }
    };

    // 显示互动布局
    private void showInteractionLayout() {
        interactionLayout.setVisibility(View.VISIBLE);
        switchInteractionUI();
    }

    /**
     * 观众申请连麦
     */
    @Override
    protected void joinQueue(CustomNotification customNotification, JSONObject json) {
        // 已经在连麦队列中，修改连麦申请的模式
        for (InteractionMember dataSource : interactionDataSource) {
            if (dataSource.getAccount().equals(customNotification.getFromAccount())) {
                if (!json.containsKey(PushLinkConstant.style)) {
                    return;
                }
                dataSource.setAvChatType(AVChatType.typeOfValue(json.getIntValue(PushLinkConstant.style)));
                interactionAdapter.notifyDataSetChanged();
                return;
            }
        }
        interactionCount++;
        saveToLocalInteractionList(customNotification.getFromAccount(), json);
        updateQueueUI();
    }

    // 主播保存互动观众
    private void saveToLocalInteractionList(String account, JSONObject jsonObject) {
        JSONObject info = (JSONObject) jsonObject.get(PushLinkConstant.info);
        String nick = info.getString(PushLinkConstant.nick);
        AVChatType style = AVChatType.typeOfValue(jsonObject.getIntValue(PushLinkConstant.style));
        if (!TextUtils.isEmpty(account)) {
            interactionDataSource.add(new InteractionMember(account, nick, AVATAR_DEFAULT, style));
        }
        interactionAdapter.notifyDataSetChanged();
    }

    // 显示互动人数
    private void updateInteractionNumbers() {
        if (interactionCount <= 0) {
            interactionCount = 0;
            interactionBtn.setText("");
            interactionBtn.setBackgroundResource(R.drawable.ic_interaction_normal);
        } else {
            interactionBtn.setBackgroundResource(R.drawable.ic_interaction_numbers);
            interactionBtn.setText(String.valueOf(interactionCount));
        }
    }

    // 有无连麦人的布局切换
    @SuppressLint("DefaultLocale")
    private void switchInteractionUI() {
        if (interactionCount <= 0) {
            noApplyText.setVisibility(View.VISIBLE);
            applyCountText.setVisibility(View.GONE);
            interactionDataSource.clear();
        } else {
            noApplyText.setVisibility(View.GONE);
            applyCountText.setVisibility(View.VISIBLE);
            applyCountText.setText(String.format("有%d人想要连线", interactionCount));
        }
        interactionAdapter.notifyDataSetChanged();
    }

    /**
     * 观众取消连麦申请
     */
    @Override
    protected void exitQueue(CustomNotification customNotification) {
        cancelLinkMember(customNotification.getFromAccount());
    }

    // 取消连麦申请 界面变化
    private void cancelLinkMember(String account) {
        removeCancelLinkMember(account);
        updateQueueUI();
    }

    // 移除取消连麦人员
    private void removeCancelLinkMember(String account) {
        if (interactionDataSource == null || interactionDataSource.isEmpty()) {
            return;
        }
        for (InteractionMember m : interactionDataSource) {
            if (m.getAccount().equals(account)) {
                interactionDataSource.remove(m);
                interactionCount--;
                break;
            }
        }
    }

    // 更新连麦列表和连麦人数
    private void updateQueueUI() {
        updateInteractionNumbers();
        switchInteractionUI();
    }

    /**
     * MemberLinkListener
     **/
    @Override
    public void onClick(InteractionMember member) {
        if (member == null) {
            return;
        }
        if (currentInteractionMember != null
                && currentInteractionMember.getAccount().equals(member.getAccount())
                && (currentInteractionMember.getMicStateEnum() == MicStateEnum.WAITING || currentInteractionMember.getMicStateEnum() == MicStateEnum.CONNECTING || currentInteractionMember.getMicStateEnum() == MicStateEnum.CONNECTED)
                && currentInteractionMember.getAvChatType().getValue() == member.getAvChatType().getValue()) {
            return;
        }
        getHandler().removeCallbacks(userJoinRunnable);
        // 选择某人进行视频连线
        if (currentInteractionMember == null || currentInteractionMember.getMicStateEnum() == MicStateEnum.NONE) {
            LogUtil.d(TAG, "link status: waiting. do link");
            doLink(member);
            getHandler().postDelayed(userJoinRunnable, USER_JOIN_OVERTIME);
        } else if (currentInteractionMember.getMicStateEnum() == MicStateEnum.CONNECTING) {
            LogUtil.d(TAG, "link status: connecting. can't click");
            // 不允许点击
        } else if (currentInteractionMember.getMicStateEnum() == MicStateEnum.CONNECTED) {
            LogUtil.d(TAG, "link status: connected. do another link");
            doAnotherLink(member);
            getHandler().postDelayed(userLeaveRunnable, USER_LEAVE_OVERTIME);
        } else if (currentInteractionMember.getMicStateEnum() == MicStateEnum.LEAVING) {
            LogUtil.d(TAG, "link status: leaving. wait delay");
            currentInteractionMember.setMicStateEnum(MicStateEnum.CONNECTING);

            nextInteractionMember = member;
            updateMemberListUI(nextInteractionMember, MicStateEnum.CONNECTING);
            getHandler().postDelayed(userLeaveRunnable, USER_LEAVE_OVERTIME);
        }
    }

    // 主播选择某人连麦
    private void doLink(InteractionMember member) {
        LogUtil.d(TAG, "do link");
        if (member == null) {
            return;
        }
        isWaiting = true;
        currentInteractionMember = member;
        updateMemberListUI(currentInteractionMember, MicStateEnum.CONNECTING);
        MicHelper.getInstance().sendLinkNotify(roomId, member);
    }

    // 连麦列表显示正在连麦中
    private void updateMemberListUI(InteractionMember member, MicStateEnum micStateEnum) {
        if (member != null)
            member.setMicStateEnum(micStateEnum);
        interactionAdapter.notifyDataSetChanged();
        interactionLayout.setVisibility(View.GONE);
    }

    // 主播正在连麦, 选择其他人连麦
    private void doAnotherLink(InteractionMember member) {
        nextInteractionMember = member;
        currentInteractionMember.setMicStateEnum(MicStateEnum.NONE);
        showLoadingLayout(nextInteractionMember);
        updateMemberListUI(nextInteractionMember, MicStateEnum.CONNECTING);
        isWaiting = true;

        ChatRoomHttpClient.getInstance().popMicLink(roomId, currentInteractionMember.getAccount(), new ChatRoomHttpClient.ChatRoomHttpCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }

            @Override
            public void onFailed(int code, String errorMsg) {
            }
        });
        // 主播先断掉正在连麦的人, 注意顺序不能改
        MicHelper.getInstance().masterBrokeMic(roomId, currentInteractionMember.getAccount());
    }

    // 显示正在连接中的等待画面
    private void showLoadingLayout(InteractionMember member) {
        audienceLoadingLayout.setVisibility(View.VISIBLE);
        connectionViewCloseBtn.setVisibility(View.VISIBLE);
        loadingClosingText.setText(R.string.video_loading);
        if (member != null) {
            onMicNick = member.getName();
            loadingNameText.setText(!TextUtils.isEmpty(onMicNick) ? member.getName() : onMicNick);
        }
    }

    // 显示连麦画面
    @Override
    protected void showConnectionView(String account, String nick, int style) {
        super.showConnectionView(account, nick, style);
        this.style = style;
        connectionViewLayout.setVisibility(View.VISIBLE);
        audienceLoadingLayout.setVisibility(View.GONE);
        livingBg.setVisibility(View.VISIBLE);
        if (liveType == LiveType.VIDEO_TYPE && style == AVChatType.VIDEO.getValue()) {
            bypassVideoRender.setVisibility(View.VISIBLE);
            audienceLivingLayout.setVisibility(View.VISIBLE);
            audioModeBypassLayout.setVisibility(View.GONE);
            AVChatManager.getInstance().setupRemoteVideoRender(account, bypassVideoRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
        } else if (style == AVChatType.AUDIO.getValue()) {
            audienceLivingLayout.setVisibility(View.GONE);
            audioModeBypassLayout.setVisibility(View.VISIBLE);
        }
    }

    // 移除互动布局中的申请连麦成员
    private void removeMemberFromList(String account) {
        if (currentInteractionMember != null){
            currentInteractionMember.setMicStateEnum(MicStateEnum.CONNECTED);
        }
        nextInteractionMember = null;
        cancelLinkMember(account);
    }

    /**
     * 断开连麦
     **/

    // 断开连麦
    @Override
    protected void doCloseInteraction() {
        if (currentInteractionMember == null) {
            return;
        }
        if (currentInteractionMember.getMicStateEnum() == MicStateEnum.CONNECTED) {
            MicHelper.getInstance().masterBrokeMic(roomId, currentInteractionMember.getAccount());
        } else if (currentInteractionMember.getMicStateEnum() == MicStateEnum.CONNECTING) {
            // 正在连麦中被关闭了,从显示队列中删除，并刷新数字
            isWaiting = false;
            for (InteractionMember member : interactionDataSource) {
                if (member.getAccount().equals(currentInteractionMember.getAccount())) {
                    interactionDataSource.remove(member);
                    interactionAdapter.notifyDataSetChanged();
                    interactionCount--;
                    updateInteractionNumbers();
                    break;
                }
            }
        }
        currentInteractionMember.setMicStateEnum(MicStateEnum.NONE);
        MicHelper.getInstance().updateMemberInChatRoomByStatus(roomId, currentInteractionMember);
    }

    // 隐藏旁路直播.移除内存队列
    @Override
    protected void resetConnectionView() {
        super.resetConnectionView();
        bypassVideoRender.setVisibility(View.GONE);
    }

    // 被观众拒绝
    @Override
    protected void rejectConnecting(String account) {
        if (currentInteractionMember != null && currentInteractionMember.getAccount().equals(account)) {
            Toast.makeText(LiveActivity.this, "被观众拒绝", Toast.LENGTH_SHORT).show();
            isWaiting = false;
            currentInteractionMember.setMicStateEnum(MicStateEnum.NONE);
            cancelLinkMember(currentInteractionMember.getAccount());
            resetConnectionView();
        }
    }

    /************************
     * AVChatStateObserver
     *****************************/
    @Override
    public void onTakeSnapshotResult(String s, boolean b, String s1) {
    }

    @Override
    public void onConnectionTypeChanged(int i) {
    }

    @Override
    public void onAVRecordingCompletion(String s, String s1) {
    }

    @Override
    public void onAudioRecordingCompletion(String s) {
    }

    @Override
    public void onLowStorageSpaceWarning(long l) {
    }

    @Override
    public void onAudioMixingProgressUpdated(long l, long l1) {
    }

    @Override
    public void onFirstVideoFrameAvailable(String s) {
        LogUtil.i("onFirstVideoFrameAvailable", s);
    }

    @Override
    public void onVideoFpsReported(String s, int i) {
    }

    @Override
    public void onLeaveChannel() {
    }

    @Override
    public void onJoinedChannel(int code, String audioFile, String videoFile, int elapsed) {
        LogUtil.d(TAG, "onJoinedChannel: " + code);
        if (code != AVChatResCode.JoinChannelCode.OK) {
            Toast.makeText(LiveActivity.this, "加入频道失败", Toast.LENGTH_SHORT).show();
            showLiveFinishLayout();
        }
    }

    @Override
    public void onUserJoined(String s) {
        // 1、主播显示旁路直播画面
        // 2、主播发送全局自定义消息告诉观众有人连麦拉
        if (currentInteractionMember == null) {
            return;
        }
        currentInteractionMember.setMicStateEnum(MicStateEnum.CONNECTED);
        isWaiting = false;
        getHandler().removeCallbacks(userJoinRunnable);
        MicHelper.getInstance().sendConnectedMicMsg(roomId, currentInteractionMember);
        MicHelper.getInstance().updateMemberInChatRoom(roomId, currentInteractionMember);
        removeMemberFromList(s);
        if (audienceLivingLayout.getVisibility() == View.VISIBLE && currentInteractionMember.getAvChatType() == AVChatType.VIDEO) {
            // 如果是已经有连麦的人，下一个连麦人上麦，不隐藏小窗口，直接切换画面
            LogUtil.d(TAG, "another one show on screen");
            AVChatManager.getInstance().setupRemoteVideoRender(s, bypassVideoRender, false, AVChatVideoScalingType.SCALE_ASPECT_BALANCED);
            updateOnMicName(currentInteractionMember.getName());
            audienceLoadingLayout.setVisibility(View.GONE);
            livingBg.setVisibility(View.VISIBLE);
        } else {
            LogUtil.d(TAG, "show someone on screen");
            showConnectionView(s, currentInteractionMember.getName(), currentInteractionMember.getAvChatType().getValue());
        }
    }

    @Override
    public void onUserLeave(String s, int i) {
        // 连麦者离开房间
        MicHelper.getInstance().popQueue(roomId, s);
        MicHelper.getInstance().sendBrokeMicMsg(roomId, s);
        getHandler().removeCallbacks(userLeaveRunnable);
        if (currentInteractionMember != null)
            currentInteractionMember.setMicStateEnum(MicStateEnum.NONE);
        if (isWaiting) {
            LogUtil.d(TAG, "on user leave, someone is waiting, do link");
            currentInteractionMember = nextInteractionMember;
            doLink(nextInteractionMember);
        } else {
            LogUtil.d(TAG, "on user leave, do close view");
            doCloseInteractionView();
        }
    }

    @Override
    public void onProtocolIncompatible(int i) {
    }

    @Override
    public void onDisconnectServer(int i) {
        releaseRtc(true, true);
        NIMClient.getService(ChatRoomService.class).exitChatRoom(roomId);
        clearChatRoom();
    }

    private int networkQuality = -1;

    @Override
    public void onNetworkQuality(String s, int i, AVChatNetworkStats avChatNetworkStats) {
        if (liveType != LiveType.VIDEO_TYPE || roomInfo == null) {
            return;
        }
        if (s.equals(NetIMCache.getAccount()) && s.equals(roomInfo.getCreator())) {
            if (networkQuality == -1) {
                networkQuality = i;
            }

            netStateImage.setVisibility(View.VISIBLE);
            switch (networkQuality) {
                case AVChatNetworkQuality.BAD:
                    netStateTipText.setText(R.string.network_bad);
                    netStateImage.setImageResource(R.drawable.ic_network_bad);
                    break;
                case AVChatNetworkQuality.POOR:
                    netStateTipText.setText(R.string.network_poor);
                    netStateImage.setImageResource(R.drawable.ic_network_poor);
                    AVChatParameters avChatParameters = new AVChatParameters();
                    avChatParameters.setRequestKey(AVChatParameters.KEY_VIDEO_QUALITY);
                    break;
                case AVChatNetworkQuality.GOOD:
                    netStateTipText.setText(R.string.network_good);
                    netStateImage.setImageResource(R.drawable.ic_network_good);
                    break;
                case AVChatNetworkQuality.EXCELLENT:
                    netStateTipText.setText(R.string.network_excellent);
                    netStateImage.setImageResource(R.drawable.ic_network_excellent);
                    break;
            }

            networkQuality = i;
        }
    }

    @Override
    public void onCallEstablished() {
        // 不使用预览功能时可以在此设置自己的画布
        LogUtil.d(TAG, "onCallEstablished");
    }

    @Override
    public void onDeviceEvent(int event, String desc) {
        if (event == AVChatDeviceEvent.VIDEO_CAMERA_SWITCH_OK) {
            notifyCapturerConfigChange();
        }
    }

    @Override
    public void onFirstVideoFrameRendered(String s) {
        LogUtil.d(TAG, "onFirstVideoFrameRendered, account:" + s);
        if (!s.equals(NetIMCache.getAccount())) {
            livingBg.setVisibility(View.GONE);
        }
    }

    @Override
    public void onVideoFrameResolutionChanged(String s, int i, int i1, int i2) {
    }

    private VideoEffect mVideoEffect;
    private boolean mHasSetFilterType = false;
    private int mCurWidth, mCurHeight;
    private boolean isUninitVideoEffect = false;// 是否销毁滤镜模块
    private int mDropFramesWhenConfigChanged = 0; //丢帧数

    @Override
    public boolean onVideoFrameFilter(AVChatVideoFrame frame, boolean maybeDualInput) {
        //如果用户不需要对视频进行美颜，这里直接返回true即可，以下示例是使用sdk提供的美颜和水印功能，用户也可以在此接入第三方的美颜sdk
        // sdk提供的滤镜模块（美颜和水印功能）要求4.3以上版本
        if (frame == null || (Build.VERSION.SDK_INT < 18)) {
            return true;
        }
        //onVideoFrameFilter回调不在主线程，VideoEffect初始化必须要和onVideoFrameFilter回调不在主线程在同一个线程
        if (mVideoEffect == null && isUninitVideoEffect == false) {
            LogUtil.d(TAG, "create Video Effect");
            mVideoEffectHandler = new Handler();
            mVideoEffect = VideoEffectFactory.getVCloudEffect();
            mVideoEffect.init(this, true, false);
            //需要delay 否则filter设置不成功
            mVideoEffect.setBeautyLevel(5);
            mVideoEffect.setFilterLevel(0.5f);
        }
        //分辨率、清晰度变化后设置丢帧数为2
        if (mCurWidth != frame.width || mCurHeight != frame.height) {
            mCurWidth = frame.width;
            mCurHeight = frame.height;
            notifyCapturerConfigChange();
        }
        if (mVideoEffect == null) {
            return true;
        }

        VideoEffect.DataFormat format = frame.format == ImageFormat.I420 ? VideoEffect.DataFormat.YUV420 : VideoEffect.DataFormat.NV21;
        boolean needMirrorData = false;
        VideoEffect.YUVData[] result;
        if ((!isBeautyBtnCancel && !isVideoBeautyOriginCurrent) || (isBeautyBtnCancel && !isVideoBeautyOriginLast)) {
            byte[] intermediate = mVideoEffect.filterBufferToRGBA(format, frame.data, frame.width, frame.height);
            if (!mHasSetFilterType) {
                mHasSetFilterType = true;
                mVideoEffect.setFilterType(VideoEffect.FilterType.nature);
                return true;
            }
            result = mVideoEffect.TOYUV420(intermediate, VideoEffect.DataFormat.RGBA, frame.width, frame.height,
                    frame.rotation, 90, frame.width, frame.height, needMirrorData, true);
        } else {
            result = mVideoEffect.TOYUV420(frame.data, format, frame.width, frame.height,
                    frame.rotation, 90, frame.width, frame.height, needMirrorData, true);
        }
        synchronized (this) {
            if (mDropFramesWhenConfigChanged-- > 0) {
                return false;
            }
        }
        System.arraycopy(result[0].data, 0, frame.data, 0, result[0].data.length);
        frame.width = result[0].width;
        frame.height = result[0].height;
        frame.dataLen = result[0].data.length;
        frame.rotation = 0;
        if (needMirrorData) {
            System.arraycopy(result[1].data, 0, frame.dataMirror, 0, result[1].data.length);
        }
        frame.dualInput = needMirrorData;
        //默认都是转换成I420
        frame.format = ImageFormat.I420;
        return true;
    }

    protected synchronized void notifyCapturerConfigChange() {
        mDropFramesWhenConfigChanged = 2;
    }

    @Override
    public boolean onAudioFrameFilter(AVChatAudioFrame avChatAudioFrame) {
        return true;
    }

    @Override
    public void onAudioDeviceChanged(int i) {
    }

    @Override
    public void onReportSpeaker(Map<String, Integer> map, int i) {
    }

    @Override
    public void onAudioMixingEvent(int i) {
    }

    @Override
    public void onSessionStats(AVChatSessionStats avChatSessionStats) {
    }

    @Override
    public void onLiveEvent(int i) {
    }

    @Override
    public void onItemFooterClick(IMMessage message) {
    }

    private void chageTxt(String txt){
        switch (txt) {
            case "a":
                targetTxt.setText("全体");
                break;
            case "p":
                targetTxt.setText("家长");
                break;
            case "t":
                targetTxt.setText("老师");
                break;
        }
    }
}


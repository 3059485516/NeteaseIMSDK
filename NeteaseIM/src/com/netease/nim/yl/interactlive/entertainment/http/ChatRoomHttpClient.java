package com.netease.nim.yl.interactlive.entertainment.http;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.yl.NetIMCache;
import com.netease.nim.yl.interactlive.base.http.NimHttpClient;
import com.netease.nim.yl.interactlive.entertainment.constant.PushLinkConstant;
import com.socks.library.KLog;

import java.util.HashMap;
import java.util.Map;

/**
 * 网易云信Demo聊天室Http客户端。第三方开发者请连接自己的应用服务器。
 * <p/>
 * Created by huangjun on 2016/2/22.
 */
public class ChatRoomHttpClient {
    private static final String TAG = ChatRoomHttpClient.class.getSimpleName();
    // header
    private static final String HEADER_KEY_APP_KEY = "appkey";
    private static final String HEADER_KEY_CONTENT_TYPE = "Content-type";
    // result
    private static final String RESULT_KEY_ERROR_MSG = "errmsg";


    public interface ChatRoomHttpCallback<T> {
        void onSuccess(T t);
        void onFailed(int code, String errorMsg);
    }

    private static ChatRoomHttpClient instance;

    public static synchronized ChatRoomHttpClient getInstance() {
        if (instance == null) {
            instance = new ChatRoomHttpClient();
        }
        return instance;
    }

    private ChatRoomHttpClient() {
        NimHttpClient.getInstance().init(NetIMCache.getContext());
    }

    /**
     * 向队列中添加连麦请求
     *
     * @param roomId   聊天室房间号
     * @param account  请求连麦用户id
     * @param ext      连麦请求附加属性
     * @param callback 请求回调
     */
    public void pushMicLink(String roomId, String account, String ext, final ChatRoomHttpCallback<Void> callback) {
        String url = NetIMCache.getHttpBaseUrl() + "Interface/addQueue";
        Map<String, String> headers = new HashMap<>(2);
        String appKey = readAppKey();
        headers.put(HEADER_KEY_APP_KEY, appKey);
        headers.put(HEADER_KEY_CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");
        StringBuilder body = new StringBuilder();
        body.append("user_name").append("=").append(account)
                .append("&").append("roomid").append("=").append(roomId)
                .append("&").append("type").append("=").append(ext)
                .append("&").append("TOKEN").append("=").append(NetIMCache.getToken());
        String bodyString = body.toString();
        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                Log.d(TAG, "pushMicLink onResponse: " + response);
                if (code != 0) {
                    LogUtil.e(TAG, "pushMicLink failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }
                try {
                    JSONObject res = JSONObject.parseObject(response);
                    int resCode = res.getInteger("code");
                    if (resCode == 0) {
                        callback.onSuccess(null);
                    } else {
                        LogUtil.e(TAG, "pushMicLink failed : code = " + code + ", errorMsg = " + res.getString(RESULT_KEY_ERROR_MSG));
                        callback.onFailed(resCode, res.getString("desc"));
                    }
                } catch (JSONException e) {
                    LogUtil.e(TAG, "NimHttpClient onResponse on JSONException, e=" + e.getMessage());
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    /**
     * 更新房间信息
     *
     * @param roomId   聊天室房间号
     * @param callback 请求回调
     */
    public void updateRoomInfo(String roomId, String meetingName, String liveCid,int isPortrait, final ChatRoomHttpCallback<Void> callback) {
        String url = NetIMCache.getHttpBaseUrl() + "Interface/updateMeeting";
        Map<String, String> headers = new HashMap<>(2);
        String appKey = readAppKey();
        headers.put(HEADER_KEY_APP_KEY, appKey);
        headers.put(HEADER_KEY_CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");
        StringBuilder body = new StringBuilder();
        Map params = new HashMap();
        params.put(PushLinkConstant.meetingName,meetingName);
        params.put(PushLinkConstant.type,"2");
        params.put(PushLinkConstant.orientation,isPortrait);
        String paramsStr = JSON.toJSONString(params);
        body.append("ext").append("=").append(paramsStr)
                .append("&").append("roomid").append("=").append(roomId)
                .append("&").append("cid").append("=").append(liveCid)
                .append("&").append("TOKEN").append("=").append(NetIMCache.getToken());
        String bodyString = body.toString();
        LogUtil.d(TAG, "updateRoomInfo: " + bodyString);

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "更新房间信息失败 : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }
                try {
                    JSONObject res = JSONObject.parseObject(response);
                    int resCode = res.getInteger("code");
                    if (resCode == 0) {
                        callback.onSuccess(null);
                    } else {
                        LogUtil.e(TAG, "更新房间信息失败 : errorMsg = " + res.getString("desc"));
                        callback.onFailed(resCode, res.getString("desc"));
                    }
                } catch (JSONException e) {
                    LogUtil.e(TAG, "NimHttpClient onResponse on JSONException, e=" + e.getMessage());
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    /**
     * 从队列中取出连麦请求
     *
     * @param roomId   聊天室房间号
     * @param account  麦序中的用户id，不填的话取出麦序中的第一个用户
     * @param callback 请求回调
     */
    public void popMicLink(String roomId, String account, final ChatRoomHttpCallback<Void> callback) {
        KLog.d(TAG, "popMicLink: ");
        String url = NetIMCache.getHttpBaseUrl() + "Interface/takeQueue";
        Map<String, String> headers = new HashMap<>(2);
        String appKey = readAppKey();
        headers.put(HEADER_KEY_APP_KEY, appKey);
        headers.put(HEADER_KEY_CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");
        StringBuilder body = new StringBuilder();
        body.append("roomid").append("=").append(roomId)
                .append("&").append("user_name").append("=").append(account)
                .append("&").append("TOKEN").append("=").append(NetIMCache.getToken());
        String bodyString = body.toString();
        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    LogUtil.e(TAG, "popMicLink failed : code = " + code + ", errorMsg = " + errorMsg);
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }
                try {
                    JSONObject res = JSONObject.parseObject(response);
                    int resCode = res.getIntValue("code");
                    if (resCode == 0) {
                        callback.onSuccess(null);
                    } else {
                        LogUtil.e(TAG, "popMicLink failed : code = " + code + ", resCode:" + resCode + ", errorMsg = " + res.getString(RESULT_KEY_ERROR_MSG));
                        callback.onFailed(resCode, res.getString("desc"));
                    }
                } catch (JSONException e) {
                    LogUtil.e(TAG, "NimHttpClient onResponse on JSONException, e=" + e.getMessage());
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    /**
     * 互动直播 聊天室 群体成员 禁言
     * @param roomid
     * @param type
     * @param callback
     */
    public void muteLive(String roomid, String type, final ChatRoomHttpCallback<Void> callback) {
        String url = NetIMCache.getHttpBaseUrl() + "Interface/muteLive";
        Map<String, String> headers = new HashMap<>(2);
        String appKey = readAppKey();
        headers.put(HEADER_KEY_APP_KEY, appKey);
        headers.put(HEADER_KEY_CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");
        StringBuilder body = new StringBuilder();
        body.append("roomid").append("=").append(roomid)
                .append("&").append("type").append("=").append(type)
                .append("&").append("TOKEN").append("=").append(NetIMCache.getToken());
        String bodyString = body.toString();
        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                    return;
                }
                try {
                    JSONObject res = JSONObject.parseObject(response);
                    int resCode = res.getIntValue("code");
                    if (resCode == 0) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailed(resCode, res.getString("desc"));
                    }
                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }


    /**
     * 选择观看对象
     * @param power
     * @param cid
     * @param callback
     */
    public void chooseTargetType(String cid, String power, final ChatRoomHttpCallback<Void> callback) {
        String url = NetIMCache.getHttpBaseUrl() + "Interface/saveLivePower";
        Map<String, String> headers = new HashMap<>(2);
        String appKey = readAppKey();
        headers.put(HEADER_KEY_APP_KEY, appKey);
        headers.put(HEADER_KEY_CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");
        StringBuilder body = new StringBuilder();
        body.append("power").append("=").append(power)
                .append("&").append("cid").append("=").append(cid);
        String bodyString = body.toString();
        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (code != 0) {
                    if (callback != null) {
                        callback.onFailed(code, errorMsg);
                    }
                }else{
                    if (callback != null) {
                        callback.onSuccess(null);
                    }
                }
            }
        });
    }


    private String readAppKey() {
        try {
            ApplicationInfo appInfo = NetIMCache.getContext().getPackageManager()
                    .getApplicationInfo(NetIMCache.getContext().getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                return appInfo.metaData.getString("com.netease.nim.appKey");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

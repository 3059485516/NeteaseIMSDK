package com.netease.nim.yl.session.action;

import android.app.Activity;
import android.content.Intent;

import com.leon.lfilepickerlibrary.LFilePicker;
import com.leon.lfilepickerlibrary.utils.Constant;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.session.constant.RequestCode;
import com.netease.nim.yl.R;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.File;
import java.util.List;

/**
 * Created by hzxuwen on 2015/6/11.
 */
public class FileAction extends BaseAction {

    public FileAction() {
        super(R.drawable.message_plus_file_selector, R.string.input_panel_file);
    }

    /**
     * **********************文件************************
     */
    private void chooseFile() {
        new LFilePicker().withActivity(getActivity())
                .withRequestCode(makeRequestCode(RequestCode.GET_LOCAL_FILE))
                .withIconStyle(Constant.ICON_STYLE_YELLOW)
                .withMutilyMode(false)
                .withTitle("选择文件")
                .start();
        //FileBrowserActivity.startActivityForResult(getActivity(), makeRequestCode(RequestCode.GET_LOCAL_FILE));
    }

    @Override
    public void onClick() {
        chooseFile();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == RequestCode.GET_LOCAL_FILE) {
            //String path = data.getStringExtra(FileBrowserActivity.EXTRA_DATA_PATH);
            List<String> list = data.getStringArrayListExtra("paths");
            if (list != null && list.size() > 0){
                String path = list.get(0);
                File file = new File(path);
                IMMessage message = MessageBuilder.createFileMessage(getAccount(), getSessionType(), file, file.getName());
                sendMessage(message);
            }
        }
    }
}

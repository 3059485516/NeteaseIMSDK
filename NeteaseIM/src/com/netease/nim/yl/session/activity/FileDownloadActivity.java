package com.netease.nim.yl.session.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.yl.R;
import com.netease.nim.yl.session.MimeTypeUtils;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.tencent.smtt.sdk.TbsReaderView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by hzxuwen on 2016/12/14.
 */
public class FileDownloadActivity extends UI implements View.OnClickListener {
    private static final String INTENT_EXTRA_DATA = "INTENT_EXTRA_DATA";
    private TextView fileNameText;
    private Button fileDownloadBtn;
    private FrameLayout frameLayout;
    private ImageView ivShare;

    private IMMessage message;
    private TbsReaderView readerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_download_activity);

        onParseIntent();
        findViews();

        updateUI();
        registerObservers(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (readerView != null) {
            readerView.onStop();
        }
        registerObservers(false);
    }

    public static void start(Context context, IMMessage message) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA_DATA, message);
        intent.setClass(context, FileDownloadActivity.class);
        context.startActivity(intent);
    }

    private void onParseIntent() {
        this.message = (IMMessage) getIntent().getSerializableExtra(INTENT_EXTRA_DATA);
    }

    private void findViews() {
        fileNameText = findView(R.id.tv_title);
        fileDownloadBtn = findView(R.id.download_btn);
        frameLayout = findViewById(R.id.frame);
        ivShare = findViewById(R.id.iv_share);

        fileDownloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOriginDataHasDownloaded(message)) {
                    return;
                }

                if ("已下载".equals(fileDownloadBtn.getText().toString())) {
                    openFile(message);
                } else {
                    downloadFile();
                }
            }
        });
    }

    private void updateUI() {
        FileAttachment attachment = (FileAttachment) message.getAttachment();
        if (attachment != null) {
            fileNameText.setText(attachment.getDisplayName());
        }

        if (isOriginDataHasDownloaded(message)) {
            onDownloadSuccess();
        } else {
            onDownloadFailed();
        }
    }

    private boolean isOriginDataHasDownloaded(final IMMessage message) {
        if (message == null) {
            return false;
        }
        FileAttachment fileAttachment = (FileAttachment) message.getAttachment();
        if (fileAttachment == null) {
            return false;
        }
        String filePath = fileAttachment.getPath();
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        if (new File(filePath).exists()) {
            return true;
        }
        return new File(String.format("%s.%s", filePath, fileAttachment.getExtension())).exists();
    }

    private void downloadFile() {
        fileDownloadBtn.setEnabled(false);
        DialogMaker.showProgressDialog(this, "loading");
        NIMClient.getService(MsgService.class).downloadAttachment(message, false);
    }

    /**
     * ********************************* 下载 ****************************************
     */

    private void registerObservers(boolean register) {
        NIMClient.getService(MsgServiceObserve.class).observeMsgStatus(statusObserver, register);
    }

    private Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage msg) {
            if (!msg.isTheSame(message) || isDestroyedCompatible()) {
                return;
            }
            message = msg;
            if (msg.getAttachStatus() == AttachStatusEnum.transferred && isOriginDataHasDownloaded(msg)) {
                DialogMaker.dismissProgressDialog();
                onDownloadSuccess();
            } else if (msg.getAttachStatus() == AttachStatusEnum.fail) {
                DialogMaker.dismissProgressDialog();
                Toast.makeText(FileDownloadActivity.this, "download failed", Toast.LENGTH_SHORT).show();
                onDownloadFailed();
            }
        }
    };

    private void onDownloadSuccess() {
        ivShare.setOnClickListener(this);
        fileDownloadBtn.setText("已下载");
        fileDownloadBtn.setBackgroundResource(R.drawable.g_white_btn_pressed);
        openFile(message);
    }

    private void onDownloadFailed() {
        fileDownloadBtn.setText("下载");
        fileDownloadBtn.setBackgroundResource(R.drawable.nim_team_create_btn_selector);
    }

    private void openFile(IMMessage msg) {
        fileDownloadBtn.setVisibility(View.GONE);
        FileAttachment fileAttachment = (FileAttachment) msg.getAttachment();
        final String extension = fileAttachment.getExtension();
        final String path = fileAttachment.getPath();
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(extension)) {
            return;
        }
        if (path.endsWith(extension)) {
            doOpen(path, extension);
        } else {
            File file = new File(getApplication().getExternalCacheDir(), "nim/file");
            if (!file.exists()) {
                file.mkdirs();
            }
            final String renameTo = file.getPath() + "/" + fileAttachment.getDisplayName();
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    fileCopy(path, renameTo);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            doOpen(renameTo, extension);
                        }
                    });
                }
            }.start();
        }
    }

    private Handler handler = new Handler();

    private void fileCopy(String fromPath, String toPath) {
        File toFile = new File(toPath);
        if (!toFile.exists()) {
            File fromFile = new File(fromPath);
            //fromFile.renameTo(toFile);
            copy(fromFile, toFile);
        }
    }

    private void copy(File source, File dest) {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputChannel != null) {
                    outputChannel.close();
                }
                if (inputChannel != null) {
                    inputChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void doOpen(String path, String type) {
        ivShare.setVisibility(View.VISIBLE);
        readerView = new TbsReaderView(this, new TbsReaderView.ReaderCallback() {
            @Override
            public void onCallBackAction(Integer integer, Object o, Object o1) {
            }
        });
        frameLayout.addView(readerView);
        Bundle bundle = new Bundle();
        bundle.putString("filePath", path);
        bundle.putString("tempPath", Environment.getExternalStorageDirectory() + File.separator + "temp");
        boolean b = readerView.preOpen(type, false);
        if (b) {
            readerView.openFile(bundle);
        }
    }

    private void share() {
        if (message == null) {
            return;
        }
        FileAttachment fileAttachment = (FileAttachment) message.getAttachment();
        String extension = fileAttachment.getExtension();
        String path = fileAttachment.getPath();
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(extension)) {
            return;
        }

        File curfile = new File(getApplication().getExternalCacheDir(), "nim/file");
        if (!curfile.exists()) {
            curfile.mkdirs();
        }
        final String renameTo = curfile.getPath() + "/" + fileAttachment.getDisplayName();
        fileCopy(path, renameTo);
        path = renameTo;

        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(this, "com.yl.lovestudy.provider", file);
                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                intent.setDataAndType(contentUri, MimeTypeUtils.getMineType(file));
                startActivity(Intent.createChooser(intent, "眯幼"));
            } else {
                Uri contentUri = Uri.fromFile(file);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                intent.setDataAndType(contentUri, MimeTypeUtils.getMineType(file));
                startActivity(Intent.createChooser(intent, "眯幼"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == ivShare) {
            share();
        }
    }
}

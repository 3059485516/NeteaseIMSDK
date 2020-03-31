package com.netease.nim.yl;

import android.text.TextUtils;
import android.widget.EditText;

import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yang Shihao
 */
public class Group implements Serializable {
    private boolean expanded = true;
    private String id;
    private String name;
    private int icon;
    private int sort = 0;
    private String tempName;
    private EditText mEditText;
    private List<NimUserInfo> friends = new ArrayList();

    public Group() {
    }

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Group(String name, int icon, int sort) {
        this.name = name;
        this.icon = icon;
        this.sort = sort;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public List<NimUserInfo> getFriends() {
        return friends;
    }

    public void setFriends(List<NimUserInfo> friends) {
        this.friends.clear();
        this.friends.addAll(friends);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getTempName() {
        return tempName;
    }

    public void setTempName(String tempName) {
        this.tempName = tempName;
    }

    public void clearGroup() {
        friends.clear();
    }

    public void addFriend(NimUserInfo info) {
        friends.add(info);
    }


    public boolean nameIsChange() {
        return !TextUtils.isEmpty(tempName) && !tempName.equals(name);
    }

    public String updateAfterName() {
        if (TextUtils.isEmpty(tempName)) {
            return name;
        }
        return tempName;
    }

    public void setEditText(EditText editText) {
        mEditText = editText;
    }
}

package com.netease.nim.yl.interactlive.entertainment.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.yl.R;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yang Shihao
 */

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.VH> {
    private List<ChatRoomMember> mMembers = new ArrayList<>();
    private ItemClickListener mItemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public void updateData(List<ChatRoomMember> members) {
        mMembers.clear();
        if (members != null) {
            mMembers.addAll(members);
        }
        notifyDataSetChanged();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_live_member, parent, false));
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (position >= mMembers.size() || position < 0) {
            return;
        }
        final ChatRoomMember chatRoomMember = mMembers.get(position);
        holder.headImageView.loadAvatar(chatRoomMember.getAvatar());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.itemClick(chatRoomMember);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMembers.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        HeadImageView headImageView;
        public VH(View itemView) {
            super(itemView);
            headImageView = (HeadImageView) itemView;
        }
    }

    public interface ItemClickListener {
        void itemClick(ChatRoomMember chatRoomMember);
    }
}

package com.naren.quire.ui.chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.naren.quire.util.sendbird.ImageUtils;
import com.sendbird.android.User;
import com.naren.quire.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple adapter that displays a list of Users.
 */
public class UserListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;


    public UserListAdapter(Context context) {
        mContext = context;
        mUsers = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_user, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((UserHolder) holder).bind(mContext, mUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void setUserList(List<? extends User> users) {
        mUsers.addAll(users);
        notifyDataSetChanged();
    }

    public void addLast(User user) {
        mUsers.add(user);
        notifyDataSetChanged();
    }

    private class UserHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private ImageView profileImage;

        public UserHolder(View itemView) {
            super(itemView);

            nameText = (TextView) itemView.findViewById(R.id.text_user_list_nickname);
            profileImage = (ImageView) itemView.findViewById(R.id.image_user_list_profile);
        }


        private void bind(final Context context, final User user) {
            nameText.setText(user.getNickname());
            ImageUtils.displayRoundImageFromUrl(context, user.getProfileUrl(), profileImage);
        }
    }
}


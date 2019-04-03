package org.nuclearfog.twidda.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.TwitterUser;

import java.util.List;

public class UserAdapter extends Adapter<UserAdapter.ItemHolder> {

    private TwitterUser mUser[];
    private OnItemClickListener mListener;
    private int font_color = 0xFFFFFFFF;
    private boolean loadImage = true;


    public UserAdapter(OnItemClickListener mListener) {
        mUser = new TwitterUser[0];
        this.mListener = mListener;
    }


    public TwitterUser getData(int pos) {
        return mUser[pos];
    }


    public void setData(@NonNull List<TwitterUser> userList) {
        mUser = userList.toArray(mUser);
    }


    public void toggleImage(boolean image) {
        loadImage = image;
    }


    public void setColor(int font_color) {
        this.font_color = font_color;
    }


    @Override
    public int getItemCount() {
        return mUser.length;
    }


    @Override
    public long getItemId(int pos) {
        return mUser[pos].getId();
    }


    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView rv = (RecyclerView) parent;
                int position = rv.getChildLayoutPosition(v);
                mListener.onItemClick(rv, position);
            }
        });
        return new ItemHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ItemHolder vh, int index) {
        TwitterUser user = mUser[index];
        vh.screenname.setText(user.getScreenname());
        vh.username.setText(user.getUsername());

        vh.screenname.setTextColor(font_color);
        vh.username.setTextColor(font_color);


        if (loadImage) {
            Picasso.get().load(user.getImageLink() + "_mini").into(vh.profileImg);
        }
        if (user.isVerified()) {
            vh.verifyIco.setVisibility(View.VISIBLE);
        } else {
            vh.verifyIco.setVisibility(View.GONE);
        }
        if (user.isLocked()) {
            vh.lockIco.setVisibility(View.VISIBLE);
        } else {
            vh.lockIco.setVisibility(View.GONE);
        }
    }


    class ItemHolder extends ViewHolder {
        final ImageView profileImg, verifyIco, lockIco;
        final TextView username, screenname;

        ItemHolder(View v) {
            super(v);
            username = v.findViewById(R.id.username_detail);
            screenname = v.findViewById(R.id.screenname_detail);
            profileImg = v.findViewById(R.id.user_profileimg);
            verifyIco = v.findViewById(R.id.verified);
            lockIco = v.findViewById(R.id.locked_profile);
        }
    }
}
package org.nuclearfog.twidda.viewadapter;

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
import org.nuclearfog.twidda.backend.listitems.TwitterUser;

import java.util.ArrayList;
import java.util.List;

public class UserRecycler extends Adapter<UserRecycler.ItemHolder> implements OnClickListener {

    private List<TwitterUser> mUser;
    private OnItemClicked mListener;
    private ViewGroup parent;
    private boolean loadImage = true;

    public UserRecycler(OnItemClicked mListener) {
        mUser = new ArrayList<>();
        this.mListener = mListener;
    }


    public void setData(List<TwitterUser> mUser) {
        this.mUser = mUser;
    }


    public List<TwitterUser> getData() {
        return mUser;
    }


    public void toggleImage(boolean image) {
        loadImage = image;
    }


    @Override
    public int getItemCount(){
        return mUser.size();
    }


    @Override
    public long getItemId(int pos) {
        return mUser.get(pos).userID;
    }


    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int index) {
        this.parent = parent;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user,parent,false);
        v.setOnClickListener(this);
        return new ItemHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ItemHolder vh, int index) {
        TwitterUser user = mUser.get(index);
        vh.screenname.setText(user.screenname);
        vh.username.setText(user.username);
        if(loadImage) {
            Picasso.with(parent.getContext()).load(user.profileImg+"_mini").into(vh.profileImg);
        }
        if(user.isVerified) {
            vh.verifyIco.setVisibility(View.VISIBLE);
        } else {
            vh.verifyIco.setVisibility(View.GONE);
        }
        if(user.isLocked) {
            vh.lockIco.setVisibility(View.VISIBLE);
        } else {
            vh.lockIco.setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View view) {
        ViewGroup p = UserRecycler.this.parent;
        RecyclerView rv = (RecyclerView) p;
        int position = rv.getChildLayoutPosition(view);
        mListener.onItemClick(view, p, position);
    }


    class ItemHolder extends ViewHolder {
        ImageView profileImg, verifyIco, lockIco;
        TextView username, screenname;
        ItemHolder(View v) {
            super(v);
            username = v.findViewById(R.id.username_detail);
            screenname = v.findViewById(R.id.screenname_detail);
            profileImg = v.findViewById(R.id.user_profileimg);
            verifyIco = v.findViewById(R.id.verified);
            lockIco = v.findViewById(R.id.locked_profile);
        }
    }


    public interface OnItemClicked {
        void onItemClick(View v, ViewGroup parent, int position);
    }
}
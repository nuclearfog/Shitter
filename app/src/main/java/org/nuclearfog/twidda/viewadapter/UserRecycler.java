package org.nuclearfog.twidda.viewadapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.UserDatabase;

public class UserRecycler extends RecyclerView.Adapter<UserRecycler.ItemHolder> implements View.OnClickListener {

    private UserDatabase mUser;
    private OnItemClicked mListener;
    private ViewGroup parent;

    public UserRecycler(UserDatabase mUser, OnItemClicked mListener) {
        this.mListener = mListener;
        this.mUser = mUser;
    }

    public UserDatabase getData(){return mUser; }


    @Override
    public int getItemCount(){
        return mUser.getSize();
    }


    @Override
    public long getItemId(int pos){
        return mUser.getUserID(pos);
    }


    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int index) {
        this.parent = parent;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.useritem, parent,false);
        v.setOnClickListener(this);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(ItemHolder vh, int index) {
        vh.screenname.setText(mUser.getScreenname(index));
        vh.username.setText(mUser.getUsername(index));
        if(mUser.loadImages()) {
            Picasso.with(parent.getContext()).load(mUser.getImageUrl(index)).into(vh.profileImg);
        }
        if(mUser.isVerified(index)) {
            vh.verifyIco.setVisibility(View.VISIBLE);
        } else {
            vh.verifyIco.setVisibility(View.INVISIBLE);
        }
        if(mUser.isLocked(index)) {
            vh.lockIco.setVisibility(View.VISIBLE);
        } else {
            vh.lockIco.setVisibility(View.INVISIBLE);
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
        public ItemHolder(View v) {
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

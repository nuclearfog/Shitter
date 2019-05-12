package org.nuclearfog.twidda.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.items.TwitterUser;

import java.lang.ref.WeakReference;
import java.util.List;

public class UserAdapter extends Adapter<UserAdapter.ItemHolder> {

    private WeakReference<OnItemClickListener> itemClickListener;
    private TwitterUser mUser[];
    private int font_color;
    private boolean loadImage;


    public UserAdapter(OnItemClickListener l) {
        itemClickListener = new WeakReference<>(l);
        mUser = new TwitterUser[0];
        font_color = 0xFFFFFFFF;
        loadImage = true;
    }


    public TwitterUser getData(int pos) {
        return mUser[pos];
    }


    public void setData(@NonNull List<TwitterUser> userList) {
        mUser = userList.toArray(new TwitterUser[0]);
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
                if (itemClickListener.get() != null)
                    itemClickListener.get().onItemClick(position);
            }
        });
        return new ItemHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ItemHolder vh, int index) {
        TwitterUser user = mUser[index];
        vh.username.setText(user.getUsername());
        vh.username.setTextColor(font_color);
        vh.screenname.setText(user.getScreenname());
        vh.screenname.setTextColor(font_color);

        if (loadImage) {
            Picasso.get().load(user.getImageLink() + "_mini").into(vh.profileImg);
        }
        if (user.isVerified()) {
            vh.username.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
        } else {
            vh.username.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        if (user.isLocked()) {
            vh.screenname.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
        } else {
            vh.screenname.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }


    class ItemHolder extends ViewHolder {
        final ImageView profileImg;
        final TextView username, screenname;

        ItemHolder(View v) {
            super(v);
            username = v.findViewById(R.id.username_detail);
            screenname = v.findViewById(R.id.screenname_detail);
            profileImg = v.findViewById(R.id.user_profileimg);
        }
    }
}
package org.nuclearfog.twidda.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class UserAdapter extends Adapter<UserAdapter.ItemHolder> {

    private WeakReference<UserClickListener> itemClickListener;
    private List<TwitterUser> users;
    private GlobalSettings settings;


    public UserAdapter(UserClickListener l, GlobalSettings settings) {
        itemClickListener = new WeakReference<>(l);
        users = new ArrayList<>();
        this.settings = settings;
    }


    @MainThread
    public void replaceAll(@NonNull List<TwitterUser> userList) {
        users.clear();
        users.addAll(userList);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return users.size();
    }


    @Override
    public long getItemId(int index) {
        return users.get(index).getId();
    }


    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        final ItemHolder vh = new ItemHolder(v);
        FontTool.setViewFontAndColor(settings, v);

        v.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener.get() != null) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION)
                        itemClickListener.get().onUserClick(users.get(position));
                }
            }
        });
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull ItemHolder vh, int index) {
        TwitterUser user = users.get(index);
        vh.username.setText(user.getUsername());
        vh.screenname.setText(user.getScreenname());
        if (settings.getImageLoad()) {
            String pbLink = user.getImageLink();
            if (!user.hasDefaultProfileImage())
                pbLink += "_mini";
            Picasso.get().load(pbLink).into(vh.profileImg);
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


    static class ItemHolder extends ViewHolder {
        final ImageView profileImg;
        final TextView username, screenname;

        ItemHolder(View v) {
            super(v);
            username = v.findViewById(R.id.username_detail);
            screenname = v.findViewById(R.id.screenname_detail);
            profileImg = v.findViewById(R.id.user_profileimg);
        }
    }


    /**
     * Listener for list click
     */
    public interface UserClickListener {

        /**
         * handle list item click
         *
         * @param user user item
         */
        void onUserClick(TwitterUser user);
    }
}
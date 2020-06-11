package org.nuclearfog.twidda.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.helper.FontTool;
import org.nuclearfog.twidda.backend.holder.UserListHolder;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_ID;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class UserAdapter extends Adapter<ViewHolder> {

    private static final int ITEM_USER = 0;
    private static final int ITEM_GAP = 1;

    private WeakReference<UserClickListener> itemClickListener;
    private List<TwitterUser> users;
    private GlobalSettings settings;

    private long nextCursor;


    public UserAdapter(UserClickListener l, GlobalSettings settings) {
        itemClickListener = new WeakReference<>(l);
        users = new ArrayList<>();
        this.settings = settings;
    }


    @MainThread
    public void setData(@NonNull UserListHolder data) {
        if (users.isEmpty() || !data.hasPrevious()) {
            if (!users.isEmpty())
                users.clear();
            users.addAll(data.getUsers());
            if (data.hasNext())
                users.add(null);
            notifyDataSetChanged();
        } else {
            int insertAt = users.size() - 1;
            int end = insertAt + data.getSize();
            users.addAll(insertAt, data.getUsers());
            if (!data.hasNext())
                users.remove(end);
            notifyItemRangeChanged(insertAt, end);
        }
        nextCursor = data.getNext();
    }


    @Override
    public int getItemCount() {
        return users.size();
    }


    @Override
    public long getItemId(int index) {
        if (users.get(index) != null)
            return users.get(index).getId();
        return NO_ID;
    }


    @Override
    public int getItemViewType(int index) {
        if (users.get(index) == null)
            return ITEM_GAP;
        return ITEM_USER;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_USER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            final ItemHolder vh = new ItemHolder(v);
            FontTool.setViewFontAndColor(settings, v);

            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener.get() != null) {
                        int position = vh.getLayoutPosition();
                        TwitterUser user = users.get(position);
                        if (position != NO_POSITION && user != null) {
                            itemClickListener.get().onUserClick(user);
                        }
                    }
                }
            });
            return vh;
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_placeholder, parent, false);
            final UserAdapter.PlaceHolder vh = new UserAdapter.PlaceHolder(v);
            vh.loadBtn.setTypeface(settings.getFontFace());
            vh.loadBtn.setTextColor(settings.getFontColor());
            vh.loadBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener.get() != null) {
                        itemClickListener.get().onFooterClick(nextCursor);
                    }
                }
            });
            return vh;
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
        TwitterUser user = users.get(index);
        if (holder instanceof ItemHolder && user != null) {
            ItemHolder vh = (ItemHolder) holder;
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

    class PlaceHolder extends ViewHolder {

        final Button loadBtn;

        PlaceHolder(@NonNull View v) {
            super(v);
            loadBtn = v.findViewById(R.id.item_placeholder);
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

        void onFooterClick(long cursor);
    }
}
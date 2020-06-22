package org.nuclearfog.twidda.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
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

import java.util.ArrayList;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.NO_ID;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 * Adapter class for user list
 *
 * @see org.nuclearfog.twidda.fragment.UserFragment
 */
public class UserAdapter extends Adapter<ViewHolder> {

    private static final int ITEM_USER = 0;
    private static final int ITEM_GAP = 1;

    private UserClickListener itemClickListener;
    private GlobalSettings settings;

    private List<TwitterUser> users;
    private long nextCursor;


    public UserAdapter(UserClickListener itemClickListener, GlobalSettings settings) {
        this.itemClickListener = itemClickListener;
        this.settings = settings;
        users = new ArrayList<>();
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
            int end = users.size() - 1;
            if (!data.hasNext())
                users.remove(end--);
            users.addAll(end, data.getUsers());
            notifyItemRangeChanged(end, data.getSize());
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
            FontTool.setViewFontAndColor(settings, v);
            final ItemHolder vh = new ItemHolder(v);
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = vh.getLayoutPosition();
                    TwitterUser user = users.get(position);
                    if (position != NO_POSITION && user != null) {
                        itemClickListener.onUserClick(user);
                    }
                }
            });
            return vh;
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_placeholder, parent, false);
            final PlaceHolder vh = new PlaceHolder(v);
            vh.loadBtn.setTypeface(settings.getFontFace());
            vh.loadBtn.setTextColor(settings.getFontColor());
            vh.loadBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onFooterClick(nextCursor);
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

            setIcon(vh.username, user.isVerified() ? R.drawable.verify : 0);
            setIcon(vh.screenname, user.isLocked() ? R.drawable.lock : 0);

            if (settings.getImageLoad()) {
                String pbLink = user.getImageLink();
                if (!user.hasDefaultProfileImage()) {
                    pbLink += "_mini";
                }
                Picasso.get().load(pbLink).error(R.drawable.no_image).into(vh.profileImg);
            }
        }
    }


    private void setIcon(TextView tv, @DrawableRes int icon) {
        tv.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
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
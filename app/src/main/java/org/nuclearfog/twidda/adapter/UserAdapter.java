package org.nuclearfog.twidda.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.holder.TwitterUserList;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.utils.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_ID;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 * Adapter class for user list
 *
 * @see org.nuclearfog.twidda.fragment.UserFragment
 */
public class UserAdapter extends Adapter<ViewHolder> {

    private static final int NO_INDEX = -1;
    private static final int ITEM_USER = 0;
    private static final int ITEM_GAP = 1;

    private final UserClickListener itemClickListener;
    private final GlobalSettings settings;

    private final List<TwitterUser> users;
    private long nextCursor;
    private int loadingIndex;


    public UserAdapter(UserClickListener itemClickListener, GlobalSettings settings) {
        this.itemClickListener = itemClickListener;
        this.settings = settings;
        users = new ArrayList<>();
        loadingIndex = NO_INDEX;
    }


    @MainThread
    public void setData(@NonNull TwitterUserList data) {
        if (users.isEmpty() || !data.hasPrevious()) {
            if (!users.isEmpty()) {
                // replace previous data
                users.clear();
            }
            users.addAll(data);
            if (data.hasNext()) {
                // add footer
                users.add(null);
            }
            notifyDataSetChanged();
        } else {
            int end = users.size() - 1;
            if (!data.hasNext()) {
                // remove footer
                users.remove(end);
                notifyItemRemoved(end);
            } else {
                disableLoading();
            }
            users.addAll(end, data);
            notifyItemRangeInserted(end, data.size());
        }
        nextCursor = data.getNext();
        loadingIndex = NO_INDEX;
    }

    /**
     * disable loading animation in footer
     */
    public void disableLoading() {
        if (loadingIndex != NO_INDEX) {
            int oldIndex = loadingIndex;
            loadingIndex = NO_INDEX;
            notifyItemChanged(oldIndex);
        }
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
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION) {
                        itemClickListener.onFooterClick(nextCursor);
                        vh.loadCircle.setVisibility(VISIBLE);
                        vh.loadBtn.setVisibility(INVISIBLE);
                        loadingIndex = position;
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

            setIcon(vh.username, user.isVerified() ? R.drawable.verify : 0);
            setIcon(vh.screenname, user.isLocked() ? R.drawable.lock : 0);

            if (settings.getImageLoad()) {
                String pbLink = user.getImageLink();
                if (!user.hasDefaultProfileImage()) {
                    pbLink += "_mini";
                }
                Picasso.get().load(pbLink).error(R.drawable.no_image).into(vh.profileImg);
            }
        } else if (holder instanceof PlaceHolder) {
            PlaceHolder vh = (PlaceHolder) holder;
            if (loadingIndex != NO_INDEX) {
                vh.loadCircle.setVisibility(VISIBLE);
                vh.loadBtn.setVisibility(INVISIBLE);
            } else {
                vh.loadCircle.setVisibility(INVISIBLE);
                vh.loadBtn.setVisibility(VISIBLE);
            }
        }
    }


    private void setIcon(TextView tv, @DrawableRes int icon) {
        tv.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
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

    static class PlaceHolder extends ViewHolder {

        final ProgressBar loadCircle;
        final Button loadBtn;

        PlaceHolder(@NonNull View v) {
            super(v);
            loadCircle = v.findViewById(R.id.placeholder_loading);
            loadBtn = v.findViewById(R.id.placeholder_button);
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
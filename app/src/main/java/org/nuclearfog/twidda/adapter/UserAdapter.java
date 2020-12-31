package org.nuclearfog.twidda.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
import org.nuclearfog.twidda.backend.items.User;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.text.NumberFormat;

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

    /**
     * index of {@link #loadingIndex} if no index is defined
     */
    private static final int NO_INDEX = -1;

    /**
     * View type for an user item
     */
    private static final int ITEM_USER = 0;

    /**
     * View type for a placeholder item
     */
    private static final int ITEM_GAP = 1;

    private UserClickListener itemClickListener;
    private GlobalSettings settings;
    private NumberFormat formatter;

    private TwitterUserList data;
    private int loadingIndex;
    private boolean userRemovable = false;


    public UserAdapter(UserClickListener itemClickListener, GlobalSettings settings) {
        this.itemClickListener = itemClickListener;
        this.settings = settings;
        formatter = NumberFormat.getIntegerInstance();
        data = new TwitterUserList();
        loadingIndex = NO_INDEX;
    }


    @MainThread
    public void setData(@NonNull TwitterUserList newData) {
        if (newData.isEmpty()) {
            if (!data.isEmpty() && data.peekLast() == null) {
                // remove footer
                data.pollLast();
            }
        } else if (data.isEmpty() || !newData.hasPrevious()) {
            data.replace(newData);
            if (newData.hasNext()) {
                // add footer
                data.add(null);
            }
            notifyDataSetChanged();
        } else {
            int end = data.size() - 1;
            if (!newData.hasNext()) {
                // remove footer
                data.remove(end);
                notifyItemRemoved(end);
            }
            data.addListAt(newData, end);
            notifyItemRangeInserted(end, newData.size());
        }
        disableLoading();
    }


    @MainThread
    public void removeUser(String username) {
        for (int pos = 0; pos < data.size(); pos++) {
            if (data.get(pos).getScreenname().equals(username)) {
                data.remove(pos);
                notifyItemRemoved(pos);
                break;
            }
        }
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public long getItemId(int index) {
        if (data.get(index) != null)
            return data.get(index).getId();
        return NO_ID;
    }


    @Override
    public int getItemViewType(int index) {
        if (data.get(index) == null)
            return ITEM_GAP;
        return ITEM_USER;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_USER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            AppStyles.setViewFontAndColor(settings, v);
            final ItemHolder vh = new ItemHolder(v);
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = vh.getLayoutPosition();
                    User user = data.get(position);
                    if (position != NO_POSITION && user != null) {
                        itemClickListener.onUserClick(user);
                    }
                }
            });
            if (userRemovable) {
                vh.delete.setVisibility(VISIBLE);
                vh.delete.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = vh.getLayoutPosition();
                        User user = data.get(position);
                        if (position != NO_POSITION && user != null) {
                            itemClickListener.onDelete(user.getScreenname());
                        }
                    }
                });
            } else {
                vh.delete.setVisibility(INVISIBLE);
            }
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
                        itemClickListener.onFooterClick(data.getNext());
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
        User user = data.get(index);
        if (holder instanceof ItemHolder && user != null) {
            ItemHolder vh = (ItemHolder) holder;
            vh.username.setText(user.getUsername());
            vh.screenname.setText(user.getScreenname());
            vh.following.setText(formatter.format(user.getFollowing()));
            vh.follower.setText(formatter.format(user.getFollower()));
            setIcon(vh.username, user.isVerified() ? R.drawable.verify : 0);
            setIcon(vh.screenname, user.isLocked() ? R.drawable.lock : 0);

            if (settings.getImageLoad()) {
                String pbLink = user.getImageLink();
                if (!user.hasDefaultProfileImage()) {
                    pbLink += settings.getImageSuffix();
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

    /**
     * enables delete button for an user item
     *
     * @param enable true to enable delete button
     */
    public void enableDeleteButton(boolean enable) {
        userRemovable = enable;
    }

    /**
     * sets the TextView icons
     *
     * @param tv   TextView to add an icon
     * @param icon icon drawable
     */
    private void setIcon(TextView tv, @DrawableRes int icon) {
        tv.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
    }

    /**
     * Holder for an user view item
     */
    private final class ItemHolder extends ViewHolder {
        final ImageView profileImg;
        final TextView username, screenname, following, follower;
        final ImageButton delete;

        ItemHolder(View v) {
            super(v);
            username = v.findViewById(R.id.username_detail);
            screenname = v.findViewById(R.id.screenname_detail);
            profileImg = v.findViewById(R.id.user_profileimg);
            delete = v.findViewById(R.id.useritem_del_user);
            following = v.findViewById(R.id.item_user_friends);
            follower = v.findViewById(R.id.item_user_follower);
        }
    }

    /**
     * Holder for a footer view
     */
    private final class PlaceHolder extends ViewHolder {
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
        void onUserClick(User user);

        /**
         * handle footer click
         *
         * @param cursor next cursor of the list
         */
        void onFooterClick(long cursor);

        /**
         * remove user from a list
         *
         * @param name screen name of the user
         */
        void onDelete(String name);
    }
}
package org.nuclearfog.twidda.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.holder.TwitterUserList;
import org.nuclearfog.twidda.backend.items.User;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.text.NumberFormat;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static android.graphics.PorterDuff.Mode.SRC_IN;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_ID;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

/**
 * Adapter class for user list
 *
 * @author nuclearfog
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
    private Drawable[] icons;

    private TwitterUserList items = new TwitterUserList();
    private NumberFormat formatter = NumberFormat.getIntegerInstance();
    private int loadingIndex = NO_INDEX;
    private boolean userRemovable = false;

    /**
     * @param context           application context
     * @param itemClickListener click listener
     */
    public UserAdapter(Context context, UserClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        settings = GlobalSettings.getInstance(context);

        TypedArray drawables = context.getResources().obtainTypedArray(R.array.user_item_icons);
        icons = new Drawable[drawables.length()];
        for (int index = 0; index < drawables.length(); index++) {
            int resId = drawables.getResourceId(index, 0);
            icons[index] = AppCompatResources.getDrawable(context, resId);
        }
        drawables.recycle();
        setIconColor();
    }

    /**
     * insert an user list depending on cursor to the top or bottom
     *
     * @param newData new userlist
     */
    @MainThread
    public void setData(@NonNull TwitterUserList newData) {
        if (newData.isEmpty()) {
            if (!items.isEmpty() && items.peekLast() == null) {
                // remove footer
                items.pollLast();
            }
        } else if (items.isEmpty() || !newData.hasPrevious()) {
            items.replace(newData);
            if (newData.hasNext()) {
                // add footer
                items.add(null);
            }
            notifyDataSetChanged();
        } else {
            int end = items.size() - 1;
            if (!newData.hasNext()) {
                // remove footer
                items.remove(end);
                notifyItemRemoved(end);
            }
            items.addListAt(newData, end);
            notifyItemRangeInserted(end, newData.size());
        }
        disableLoading();
    }

    /**
     * update user information
     *
     * @param user User update
     */
    @MainThread
    public void updateUser(User user) {
        int index = items.indexOf(user);
        if (index >= 0) {
            items.set(index, user);
            notifyItemChanged(index);
        }
    }

    /**
     * remove user from adapter
     *
     * @param username User to remove
     */
    @MainThread
    public void removeUser(String username) {
        for (int pos = 0; pos < items.size(); pos++) {
            if (items.get(pos).getScreenname().equals(username)) {
                items.remove(pos);
                notifyItemRemoved(pos);
                break;
            }
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }


    @Override
    public long getItemId(int index) {
        if (items.get(index) != null)
            return items.get(index).getId();
        return NO_ID;
    }


    @Override
    public int getItemViewType(int index) {
        if (items.get(index) == null)
            return ITEM_GAP;
        return ITEM_USER;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_USER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            final ItemHolder vh = new ItemHolder(v, settings);
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = vh.getLayoutPosition();
                    User user = items.get(position);
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
                        User user = items.get(position);
                        if (position != NO_POSITION && user != null) {
                            itemClickListener.onDelete(user.getScreenname());
                        }
                    }
                });
            } else {
                vh.delete.setVisibility(GONE);
            }
            return vh;
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_placeholder, parent, false);
            final PlaceHolder vh = new PlaceHolder(v, settings);
            vh.loadBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION) {
                        itemClickListener.onFooterClick(items.getNext());
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
        User user = items.get(index);
        if (holder instanceof ItemHolder && user != null) {
            ItemHolder vh = (ItemHolder) holder;
            vh.textViews[0].setText(user.getUsername());
            vh.textViews[1].setText(user.getScreenname());
            vh.textViews[2].setText(formatter.format(user.getFollowing()));
            vh.textViews[3].setText(formatter.format(user.getFollower()));
            if (user.isVerified()) {
                setIcon(vh.textViews[0], icons[0]);
            } else {
                setIcon(vh.textViews[0], null);
            }
            if (user.isLocked()) {
                setIcon(vh.textViews[1], icons[1]);
            } else {
                setIcon(vh.textViews[1], null);
            }
            if (settings.getImageLoad() && user.hasProfileImage()) {
                String pbLink = user.getImageLink();
                if (!user.hasDefaultProfileImage())
                    pbLink += settings.getImageSuffix();
                Picasso.get().load(pbLink).transform(new RoundedCornersTransformation(2, 0))
                        .error(R.drawable.no_image).into(vh.profileImg);
            } else {
                vh.profileImg.setImageResource(0);
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
    private void setIcon(TextView tv, @Nullable Drawable icon) {
        if (icon != null)
            icon = icon.mutate();
        tv.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
    }

    /**
     * color icon drawables
     */
    private void setIconColor() {
        for (Drawable icon : icons) {
            icon.setColorFilter(settings.getIconColor(), SRC_IN);
        }
    }

    /**
     * Holder for an user view item
     */
    private final class ItemHolder extends ViewHolder {

        final TextView[] textViews = new TextView[4];
        final ImageView profileImg;
        final ImageButton delete;

        ItemHolder(View v, GlobalSettings settings) {
            super(v);
            CardView background = (CardView) v;
            textViews[0] = v.findViewById(R.id.username_detail);
            textViews[1] = v.findViewById(R.id.screenname_detail);
            textViews[2] = v.findViewById(R.id.item_user_friends);
            textViews[3] = v.findViewById(R.id.item_user_follower);
            profileImg = v.findViewById(R.id.user_profileimg);
            delete = v.findViewById(R.id.useritem_del_user);

            for (TextView tv : textViews) {
                tv.setTextColor(settings.getFontColor());
                tv.setTypeface(settings.getFontFace());
            }
            background.setCardBackgroundColor(settings.getCardColor());
            textViews[2].setCompoundDrawablesWithIntrinsicBounds(icons[2], null, null, null);
            textViews[3].setCompoundDrawablesWithIntrinsicBounds(icons[3], null, null, null);
            delete.setImageResource(R.drawable.cross);
        }
    }

    /**
     * Holder for a footer view
     */
    private final class PlaceHolder extends ViewHolder {
        final ProgressBar loadCircle;
        final Button loadBtn;

        PlaceHolder(@NonNull View v, GlobalSettings settings) {
            super(v);
            CardView background = (CardView) v;
            loadCircle = v.findViewById(R.id.placeholder_loading);
            loadBtn = v.findViewById(R.id.placeholder_button);

            background.setCardBackgroundColor(settings.getCardColor());
            AppStyles.setProgressColor(loadCircle, settings.getHighlightColor());
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
package org.nuclearfog.twidda.adapter;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.holder.Footer;
import org.nuclearfog.twidda.adapter.holder.ListHolder;
import org.nuclearfog.twidda.backend.lists.UserLists;
import org.nuclearfog.twidda.backend.model.TwitterList;
import org.nuclearfog.twidda.backend.model.User;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.fragment.UserListFragment;

import java.text.NumberFormat;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static org.nuclearfog.twidda.backend.utils.StringTools.formatCreationTime;

/**
 * Adapter class for user lists
 *
 * @author nuclearfog
 * @see UserListFragment
 */
public class ListAdapter extends Adapter<ViewHolder> {

    /**
     * indicator if there is no footer
     */
    private static final int NO_LOADING = -1;

    /**
     * View type for a footer
     */
    private static final int ITEM_FOOTER = 0;

    /**
     * View type for an userlist item
     */
    private static final int ITEM_LIST = 1;

    /**
     * locale specific number format
     */
    private static final NumberFormat NUM_FORMAT = NumberFormat.getIntegerInstance();

    private ListClickListener listener;
    private GlobalSettings settings;

    private UserLists data = new UserLists();
    private int loadingIndex = NO_LOADING;

    /**
     * @param settings app settings for theme
     * @param listener item click listener
     */
    public ListAdapter(GlobalSettings settings, ListClickListener listener) {
        this.listener = listener;
        this.settings = settings;
    }

    /**
     * adds new data to the list
     *
     * @param newData new list to add
     */
    @MainThread
    public void setData(UserLists newData) {
        disableLoading();
        if (newData.isEmpty()) {
            if (!data.isEmpty() && data.peekLast() == null) {
                // remove footer
                int end = data.size() - 1;
                data.remove(end);
                notifyItemRemoved(end);
            }
        } else if (data.isEmpty() || !newData.hasPrevious()) {
            data.replace(newData);
            if (data.hasNext()) {
                // Add footer
                data.add(null);
            }
            notifyDataSetChanged();
        } else {
            int end = data.size() - 1;
            if (!data.hasNext()) {
                // remove footer
                data.remove(end);
                notifyItemRemoved(end);
            }
            data.addAt(newData, end);
            notifyItemRangeInserted(end, newData.size());
        }
    }


    /**
     * update a single item
     *
     * @param list updated list
     */
    @MainThread
    public void updateItem(TwitterList list) {
        int index = data.indexOf(list);
        if (index >= 0) {
            data.set(index, list);
            notifyItemChanged(index);
        }
    }

    /**
     * remove user list item from list
     *
     * @param itemId user list id to remove
     */
    @MainThread
    public void removeItem(long itemId) {
        int index = data.removeItem(itemId);
        if (index >= 0) {
            notifyItemRemoved(index);
        }
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public int getItemViewType(int position) {
        if (data.get(position) == null)
            return ITEM_FOOTER;
        return ITEM_LIST;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_LIST) {
            final ListHolder itemHolder = new ListHolder(parent, settings);
            itemHolder.profile_img.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = itemHolder.getLayoutPosition();
                    if (position != NO_POSITION) {
                        TwitterList item = data.get(position);
                        if (item != null) {
                            listener.onProfileClick(item.getListOwner());
                        }
                    }
                }
            });
            itemHolder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = itemHolder.getLayoutPosition();
                    if (position != NO_POSITION) {
                        TwitterList list = data.get(position);
                        listener.onListClick(list);
                    }
                }
            });
            return itemHolder;
        } else {
            final Footer footer = new Footer(parent, settings, false);
            footer.loadBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = footer.getLayoutPosition();
                    if (position != NO_POSITION) {
                        boolean actionPerformed = listener.onFooterClick(data.getNext());
                        if (actionPerformed) {
                            footer.setLoading(true);
                            loadingIndex = position;
                        }
                    }
                }
            });
            return footer;
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
        if (holder instanceof ListHolder) {
            ListHolder vh = (ListHolder) holder;
            TwitterList item = data.get(index);
            if (item != null) {
                User owner = item.getListOwner();
                vh.textViews[0].setText(item.getTitle());
                vh.textViews[1].setText(item.getDescription());
                vh.textViews[2].setText(owner.getUsername());
                vh.textViews[3].setText(owner.getScreenname());
                vh.textViews[4].setText(formatCreationTime(item.getCreatedAt()));
                vh.textViews[5].setText(NUM_FORMAT.format(item.getMemberCount()));
                vh.textViews[6].setText(NUM_FORMAT.format(item.getSubscriberCount()));
                if (settings.imagesEnabled() && owner.hasProfileImage()) {
                    String pbLink = owner.getImageLink();
                    if (!owner.hasDefaultProfileImage())
                        pbLink += settings.getImageSuffix();
                    Picasso.get().load(pbLink).transform(new RoundedCornersTransformation(3, 0))
                            .error(R.drawable.no_image).into(vh.profile_img);
                } else {
                    vh.profile_img.setImageResource(0);
                }
                if (!item.isListOwner() && item.isFollowing()) {
                    vh.icons[6].setVisibility(VISIBLE);
                    vh.textViews[7].setVisibility(VISIBLE);
                } else {
                    vh.icons[6].setVisibility(GONE);
                    vh.textViews[7].setVisibility(GONE);
                }
                if (owner.isVerified()) {
                    vh.icons[0].setVisibility(VISIBLE);
                } else {
                    vh.icons[0].setVisibility(GONE);
                }
                if (owner.isLocked()) {
                    vh.icons[1].setVisibility(VISIBLE);
                } else {
                    vh.icons[1].setVisibility(GONE);
                }
                if (item.isPrivate()) {
                    vh.icons[5].setVisibility(VISIBLE);
                } else {
                    vh.icons[5].setVisibility(GONE);
                }
            }
        } else if (holder instanceof Footer) {
            Footer footer = (Footer) holder;
            footer.setLoading(loadingIndex != NO_LOADING);
        }
    }

    /**
     * disable loading animation in footer
     */
    public void disableLoading() {
        if (loadingIndex != NO_LOADING) {
            int oldIndex = loadingIndex;
            loadingIndex = NO_LOADING;
            notifyItemChanged(oldIndex);
        }
    }

    /**
     * Listener for an item
     */
    public interface ListClickListener {

        /**
         * called when an item is clicked
         *
         * @param listItem Item data and information
         */
        void onListClick(TwitterList listItem);

        /**
         * called when the profile image of the owner was clicked
         *
         * @param user user information
         */
        void onProfileClick(User user);

        /**
         * called when the footer is clicked
         *
         * @param cursor next cursor of the list
         */
        boolean onFooterClick(long cursor);
    }
}
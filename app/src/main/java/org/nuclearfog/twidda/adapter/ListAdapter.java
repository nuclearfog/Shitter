package org.nuclearfog.twidda.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.holder.UserListList;
import org.nuclearfog.twidda.backend.items.TwitterList;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.backend.utils.FontTool;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.fragment.UserListFragment;

import java.text.NumberFormat;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static org.nuclearfog.twidda.backend.utils.TimeString.getTimeString;

/**
 * Adapter class for user lists
 *
 * @see UserListFragment
 */
public class ListAdapter extends Adapter<ViewHolder> {

    private static final int NO_LOADING = -1;
    private static final int ITEM_FOOTER = 0;
    private static final int ITEM_LIST = 1;

    private ListClickListener listener;
    private NumberFormat formatter;
    private GlobalSettings settings;

    private UserListList data;
    private int loadingIndex;


    public ListAdapter(ListClickListener listener, GlobalSettings settings) {
        this.listener = listener;
        this.settings = settings;
        formatter = NumberFormat.getIntegerInstance();
        data = new UserListList();
    }

    /**
     * adds new data to the list
     *
     * @param newData new list to add
     */
    @MainThread
    public void setData(UserListList newData) {
        if (newData.isEmpty()) {
            if (!data.isEmpty() && data.peekLast() == null) {
                // remove footer
                data.pollLast();
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
            data.addListAt(newData, end);
            notifyItemRangeInserted(end, newData.size());
        }
        disableLoading();
    }

    /**
     * update a list item
     *
     * @param item new userlist item
     */
    @MainThread
    public void updateItem(TwitterList item) {
        int index = data.indexOf(item);
        if (index != -1) {
            data.set(index, item);
            notifyItemChanged(index);
        }
    }

    /**
     * remove userlist item from list
     *
     * @param itemId userlist id to remove
     */
    @MainThread
    public void removeItem(long itemId) {
        for (int index = 0; index < data.size(); index++) {
            if (data.get(index).getId() == itemId) {
                data.remove(index);
                notifyItemRemoved(index);
                break;
            }
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
            final ListHolder vh = new ListHolder(v);
            FontTool.setViewFontAndColor(settings, v);
            vh.pb_image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION) {
                        TwitterUser user = data.get(position).getListOwner();
                        listener.onProfileClick(user);
                    }
                }
            });
            v.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION) {
                        TwitterList list = data.get(position);
                        listener.onListClick(list);
                    }
                }
            });
            return vh;
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_placeholder, parent, false);
            final PlaceHolder ph = new PlaceHolder(v);
            ph.loadBtn.setTypeface(settings.getFontFace());
            ph.loadBtn.setTextColor(settings.getFontColor());
            ph.loadBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = ph.getLayoutPosition();
                    if (position != NO_POSITION) {
                        listener.onFooterClick(data.getNext());
                        ph.loadCircle.setVisibility(VISIBLE);
                        ph.loadBtn.setVisibility(INVISIBLE);
                        loadingIndex = position;
                    }
                }
            });
            return ph;
        }
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int index) {
        if (holder instanceof ListHolder) {
            ListHolder vh = (ListHolder) holder;
            TwitterList item = data.get(index);
            TwitterUser owner = item.getListOwner();
            vh.title.setText(item.getTitle());
            vh.username.setText(owner.getUsername());
            vh.screenname.setText(owner.getScreenname());
            vh.description.setText(item.getDescription());
            vh.createdAt.setText(getTimeString(item.getCreatedAt()));
            vh.memberCount.setText(formatter.format(item.getMemberCount()));
            vh.subscriberCount.setText(formatter.format(item.getSubscriberCount()));
            if (settings.getImageLoad()) {
                String pbLink = owner.getImageLink();
                if (!owner.hasDefaultProfileImage()) {
                    pbLink += settings.getImageSuffix();
                }
                Picasso.get().load(pbLink).error(R.drawable.no_image).into(vh.pb_image);
            }
            if (!item.isListOwner() && item.isFollowing()) {
                vh.followIndicator.setVisibility(VISIBLE);
            } else {
                vh.followIndicator.setVisibility(GONE);
            }
            if (item.isPrivate()) {
                vh.title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
            } else {
                vh.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            if (owner.isVerified()) {
                vh.username.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
            } else {
                vh.username.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            if (owner.isLocked()) {
                vh.screenname.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
            } else {
                vh.screenname.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        } else if (holder instanceof PlaceHolder) {
            PlaceHolder placeHolder = (PlaceHolder) holder;
            if (loadingIndex != NO_LOADING) {
                placeHolder.loadCircle.setVisibility(VISIBLE);
                placeHolder.loadBtn.setVisibility(INVISIBLE);
            } else {
                placeHolder.loadCircle.setVisibility(INVISIBLE);
                placeHolder.loadBtn.setVisibility(VISIBLE);
            }
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
     * view holder class for an user list item
     */
    private final class ListHolder extends ViewHolder {
        final ImageView pb_image;
        final TextView title, username, screenname, description, createdAt;
        final TextView memberCount, subscriberCount, followIndicator;

        ListHolder(View v) {
            super(v);
            pb_image = v.findViewById(R.id.list_owner_profile);
            followIndicator = v.findViewById(R.id.list_action);
            title = v.findViewById(R.id.list_title);
            username = v.findViewById(R.id.list_ownername);
            screenname = v.findViewById(R.id.list_screenname);
            description = v.findViewById(R.id.list_description);
            createdAt = v.findViewById(R.id.list_createdat);
            memberCount = v.findViewById(R.id.list_member);
            subscriberCount = v.findViewById(R.id.list_subscriber);
        }
    }

    /**
     * view holder class for a footer view
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
        void onProfileClick(TwitterUser user);

        /**
         * called when the footer is clicked
         *
         * @param cursor next cursor of the list
         */
        void onFooterClick(long cursor);
    }
}
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

import java.text.NumberFormat;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static org.nuclearfog.twidda.backend.utils.TimeString.getTimeString;

/**
 * Adapter class for user lists
 *
 * @see org.nuclearfog.twidda.fragment.ListFragment
 */
public class ListAdapter extends Adapter<ViewHolder> {

    private static final int NO_LOADING = -1;
    private static final int ITEM_FOOTER = 0;
    private static final int ITEM_LIST = 1;

    private final ListClickListener listener;
    private final NumberFormat formatter;
    private final GlobalSettings settings;

    private final UserListList data;
    private int loadingIndex;


    public ListAdapter(ListClickListener listener, GlobalSettings settings) {
        this.listener = listener;
        this.settings = settings;
        formatter = NumberFormat.getIntegerInstance();
        data = new UserListList();
    }


    @MainThread
    public void setData(UserListList newData) {
        if (data.isEmpty() || !newData.hasPrevious()) {
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


    @MainThread
    public void updateItem(TwitterList newItem) {
        int index = data.indexOf(newItem);
        if (index != -1) {
            data.set(index, newItem);
            notifyItemChanged(index);
        }
    }


    @MainThread
    public void removeItem(long id) {
        int pos = -1;
        for (int index = 0; index < data.size() && pos < 0; index++) {
            if (data.get(index).getId() == id) {
                data.remove(index);
                pos = index;
            }
        }
        if (pos != -1) {
            notifyItemRemoved(pos);
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
                        listener.onClick(data.get(position), ListClickListener.Action.PROFILE);
                    }
                }
            });
            vh.followList.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION) {
                        listener.onClick(data.get(position), ListClickListener.Action.FOLLOW);
                    }
                }
            });
            vh.deleteList.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION) {
                        listener.onClick(data.get(position), ListClickListener.Action.DELETE);
                    }
                }
            });
            vh.subscriberCount.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION) {
                        listener.onClick(data.get(position), ListClickListener.Action.SUBSCRIBER);
                    }
                }
            });
            vh.memberCount.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION) {
                        listener.onClick(data.get(position), ListClickListener.Action.MEMBER);
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
            vh.ownername.setText(owner.getScreenname());
            vh.description.setText(item.getDescription());
            vh.createdAt.setText(getTimeString(item.getCreatedAt()));
            vh.memberCount.setText(formatter.format(item.getMemberCount()));
            vh.subscriberCount.setText(formatter.format(item.getSubscriberCount()));
            if (settings.getImageLoad()) {
                String pbLink = owner.getImageLink();
                if (!owner.hasDefaultProfileImage()) {
                    pbLink += "_mini";
                }
                Picasso.get().load(pbLink).error(R.drawable.no_image).into(vh.pb_image);
            }
            if (item.isFollowing()) {
                vh.followList.setText(R.string.user_unfollow);
            } else {
                vh.followList.setText(R.string.user_follow);
            }
            if (item.isListOwner()) {
                vh.followList.setVisibility(VISIBLE);
                vh.deleteList.setVisibility(GONE);
            } else {
                vh.followList.setVisibility(GONE);
                vh.deleteList.setVisibility(VISIBLE);
            }
            if (item.isPrivate()) {
                vh.title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
            } else {
                vh.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
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


    static class ListHolder extends ViewHolder {
        final ImageView pb_image;
        final Button followList, deleteList;
        final TextView title, ownername, description, createdAt;
        final TextView memberCount, subscriberCount;

        ListHolder(View v) {
            super(v);
            pb_image = v.findViewById(R.id.list_owner_profile);
            followList = v.findViewById(R.id.list_follow);
            deleteList = v.findViewById(R.id.list_delete);
            title = v.findViewById(R.id.list_title);
            ownername = v.findViewById(R.id.list_ownername);
            description = v.findViewById(R.id.list_description);
            createdAt = v.findViewById(R.id.list_createdat);
            memberCount = v.findViewById(R.id.list_member);
            subscriberCount = v.findViewById(R.id.list_subscriber);
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
     * Listener for an item
     */
    public interface ListClickListener {

        enum Action {
            PROFILE,
            FOLLOW,
            SUBSCRIBER,
            MEMBER,
            DELETE
        }

        /**
         * called when an item is clicked
         *
         * @param listItem Item data and information
         * @param action   which button was clicked
         */
        void onClick(TwitterList listItem, Action action);

        /**
         * called when the footer is clicked
         *
         * @param cursor next cursor of the list
         */
        void onFooterClick(long cursor);
    }
}
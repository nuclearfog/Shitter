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
import org.nuclearfog.twidda.backend.helper.StringTools;
import org.nuclearfog.twidda.backend.items.TwitterList;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class ListAdapter extends Adapter<ListAdapter.ListHolder> {

    private WeakReference<ListClickListener> listener;
    private List<TwitterList> data;
    private NumberFormat formatter;
    private GlobalSettings settings;


    public ListAdapter(ListClickListener l, GlobalSettings settings) {
        data = new ArrayList<>();
        listener = new WeakReference<>(l);
        formatter = NumberFormat.getIntegerInstance();
        this.settings = settings;
    }


    @MainThread
    public void setData(List<TwitterList> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
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
        if (pos != -1)
            notifyItemRemoved(pos);
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    @NonNull
    @Override
    public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        final ListHolder vh = new ListHolder(v);
        FontTool.setViewFontAndColor(settings, v);

        vh.pb_image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener.get() != null) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION) {
                        listener.get().onClick(data.get(position), ListClickListener.Action.PROFILE);
                    }
                }
            }
        });
        vh.followList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener.get() != null) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION) {
                        listener.get().onClick(data.get(position), ListClickListener.Action.FOLLOW);
                    }
                }
            }
        });
        vh.deleteList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener.get() != null) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION) {
                        listener.get().onClick(data.get(position), ListClickListener.Action.DELETE);
                    }
                }
            }
        });
        vh.subscriberCount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener.get() != null) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION) {
                        listener.get().onClick(data.get(position), ListClickListener.Action.SUBSCRIBER);
                    }
                }
            }
        });
        vh.memberCount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener.get() != null) {
                    int position = vh.getLayoutPosition();
                    if (position != NO_POSITION) {
                        listener.get().onClick(data.get(position), ListClickListener.Action.MEMBER);
                    }
                }
            }
        });
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull ListHolder vh, int index) {
        TwitterList item = data.get(index);
        TwitterUser owner = item.getListOwner();
        vh.title.setText(item.getTitle());
        vh.ownername.setText(owner.getScreenname());
        vh.description.setText(item.getDescription());
        vh.createdAt.setText(StringTools.getTimeString(item.getCreatedAt()));
        vh.memberCount.setText(formatter.format(item.getMemberCount()));
        vh.subscriberCount.setText(formatter.format(item.getSubscriberCount()));
        if (settings.getImageLoad())
            Picasso.get().load(owner.getImageLink() + "_mini").into(vh.pb_image);
        if (item.isFollowing())
            vh.followList.setText(R.string.unfollow);
        else
            vh.followList.setText(R.string.follow);
        if (item.isListOwner()) {
            vh.followList.setVisibility(VISIBLE);
            vh.deleteList.setVisibility(GONE);
        } else {
            vh.followList.setVisibility(GONE);
            vh.deleteList.setVisibility(VISIBLE);
        }
        if (item.isPrivate())
            vh.title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
        else
            vh.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
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


    public interface ListClickListener {

        enum Action {
            PROFILE,
            FOLLOW,
            SUBSCRIBER,
            MEMBER,
            DELETE
        }

        void onClick(TwitterList listItem, Action action);
    }
}
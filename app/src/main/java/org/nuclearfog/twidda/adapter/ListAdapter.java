package org.nuclearfog.twidda.adapter;

import android.graphics.Color;
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
import org.nuclearfog.twidda.backend.helper.StringTools;
import org.nuclearfog.twidda.backend.items.TwitterList;
import org.nuclearfog.twidda.backend.items.TwitterUser;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends Adapter<ListAdapter.ListHolder> {

    private WeakReference<ListClickListener> listener;
    private List<TwitterList> data;
    private NumberFormat formatter;
    private int fontColor;

    public ListAdapter(ListClickListener l) {
        data = new ArrayList<>();
        listener = new WeakReference<>(l);
        formatter = NumberFormat.getIntegerInstance();
        fontColor = Color.WHITE;
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

    public void setColor(int fontColor) {
        this.fontColor = fontColor;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @NonNull
    @Override
    public ListHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        return new ListHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ListHolder vh, final int index) {
        final TwitterList item = data.get(index);
        final TwitterUser owner = item.getListOwner();
        vh.title.setText(item.getShortName());
        vh.ownername.setText(owner.getScreenname());
        vh.description.setText(item.getDescription());
        vh.createdAt.setText(StringTools.getTimeString(item.getCreatedAt()));
        vh.memberCount.setText(formatter.format(item.getMemberCount()));
        vh.subscriberCount.setText(formatter.format(item.getSubscriberCount()));
        vh.title.setTextColor(fontColor);
        vh.ownername.setTextColor(fontColor);
        vh.description.setTextColor(fontColor);
        vh.createdAt.setTextColor(fontColor);
        Picasso.get().load(owner.getImageLink() + "_mini").into(vh.pb_image);
        vh.pb_image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener.get() != null)
                    listener.get().onClick(owner.getId(), item.getShortName(), ListClickListener.Action.PROFILE);
            }
        });
        vh.followList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener.get() != null)
                    listener.get().onClick(item.getId(), item.getShortName(), ListClickListener.Action.FOLLOW);
            }
        });
        vh.subscriberCount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener.get() != null)
                    listener.get().onClick(item.getId(), item.getShortName(), ListClickListener.Action.SUBSCRIBER);
            }
        });
        vh.memberCount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener.get() != null)
                    listener.get().onClick(item.getId(), item.getShortName(), ListClickListener.Action.MEMBER);
            }
        });
        if (item.isFollowing())
            vh.followList.setText(R.string.unfollow);
        else
            vh.followList.setText(R.string.follow);
    }

    class ListHolder extends ViewHolder {
        final ImageView pb_image;
        final Button followList;
        final TextView title, ownername, description, createdAt;
        final TextView memberCount, subscriberCount;

        ListHolder(View v) {
            super(v);
            pb_image = v.findViewById(R.id.list_owner_profile);
            followList = v.findViewById(R.id.list_follow);
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
            MEMBER
        }

        void onClick(long id, String name, Action action);
    }
}
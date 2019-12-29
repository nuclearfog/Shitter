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
import org.nuclearfog.twidda.backend.helper.StringTools;
import org.nuclearfog.twidda.backend.items.TwitterList;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

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


    @Override
    public int getItemCount() {
        return data.size();
    }


    @NonNull
    @Override
    public ListHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        ListHolder vh = new ListHolder(v);
        vh.title.setTextColor(settings.getFontColor());
        vh.ownername.setTextColor(settings.getFontColor());
        vh.description.setTextColor(settings.getFontColor());
        vh.createdAt.setTextColor(settings.getFontColor());
        vh.title.setTypeface(settings.getFontFace());
        vh.ownername.setTypeface(settings.getFontFace());
        vh.description.setTypeface(settings.getFontFace());
        vh.createdAt.setTypeface(settings.getFontFace());
        vh.memberCount.setTypeface(settings.getFontFace());
        vh.subscriberCount.setTypeface(settings.getFontFace());
        vh.followList.setTypeface(settings.getFontFace());
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull ListHolder vh, final int index) {
        final TwitterList item = data.get(index);
        final TwitterUser owner = item.getListOwner();
        vh.title.setText(item.getTitle());
        vh.ownername.setText(owner.getScreenname());
        vh.description.setText(item.getDescription());
        vh.createdAt.setText(StringTools.getTimeString(item.getCreatedAt()));
        vh.memberCount.setText(formatter.format(item.getMemberCount()));
        vh.subscriberCount.setText(formatter.format(item.getSubscriberCount()));
        if (settings.getImageLoad())
            Picasso.get().load(owner.getImageLink() + "_mini").into(vh.pb_image);
        vh.pb_image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener.get() != null)
                    listener.get().onClick(owner.getId(), item.getTitle(), ListClickListener.Action.PROFILE);
            }
        });
        vh.followList.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener.get() != null)
                    listener.get().onClick(item.getId(), item.getTitle(), ListClickListener.Action.FOLLOW);
            }
        });
        vh.subscriberCount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener.get() != null)
                    listener.get().onClick(item.getId(), item.getTitle(), ListClickListener.Action.SUBSCRIBER);
            }
        });
        vh.memberCount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener.get() != null)
                    listener.get().onClick(item.getId(), item.getTitle(), ListClickListener.Action.MEMBER);
            }
        });
        if (item.isFollowing())
            vh.followList.setText(R.string.unfollow);
        else
            vh.followList.setText(R.string.follow);
        if (item.enableFollow())
            vh.followList.setVisibility(VISIBLE);
        else
            vh.followList.setVisibility(INVISIBLE);
        if (item.isPrivate())
            vh.title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
        else
            vh.title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
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
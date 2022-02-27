package org.nuclearfog.twidda.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static org.nuclearfog.twidda.backend.utils.StringTools.formatCreationTime;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.holder.Footer;
import org.nuclearfog.twidda.adapter.holder.MessageHolder;
import org.nuclearfog.twidda.backend.lists.Directmessages;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.DirectMessage;
import org.nuclearfog.twidda.model.User;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * custom {@link androidx.recyclerview.widget.RecyclerView} adapter implementation to show directmessages
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.fragments.MessageFragment
 */
public class MessageAdapter extends Adapter<ViewHolder> {

    /**
     * index of {@link #loadingIndex} if no index is defined
     */
    private static final int NO_LOADING = -1;

    /**
     * view type of a message item
     */
    private static final int TYPE_MESSAGE = 0;

    /**
     * view type of a footer item
     */
    private static final int TYPE_FOOTER = 1;

    private OnMessageClickListener itemClickListener;
    private GlobalSettings settings;
    private Resources resources;
    private Picasso picasso;

    private Directmessages data = new Directmessages(null, null);
    private int loadingIndex = NO_LOADING;

    /**
     * @param itemClickListener click listener
     */
    public MessageAdapter(Context context, OnMessageClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
        settings = GlobalSettings.getInstance(context);
        picasso = PicassoBuilder.get(context);
        resources = context.getResources();
    }

    /**
     * set messages
     *
     * @param newData new message list
     */
    @MainThread
    public void setData(Directmessages newData) {
        disableLoading();
        if (newData.isEmpty()) {
            if (!data.isEmpty() && data.peekLast() == null) {
                int end = data.size() - 1;
                data.remove(end);
                notifyItemRemoved(end);
            }
        } else if (data.isEmpty() || !newData.hasPrev()) {
            data.replaceAll(newData);
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
            data.addAt(newData, end);
            notifyItemRangeInserted(end, newData.size());
        }
    }

    /**
     * Remove a single item from list if found
     *
     * @param id message ID
     */
    @MainThread
    public void remove(long id) {
        int pos = data.removeItem(id);
        if (pos >= 0) {
            notifyItemRemoved(pos);
        }
    }


    @Override
    public long getItemId(int index) {
        DirectMessage message = data.get(index);
        if (message != null)
            return message.getId();
        return -1;
    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public int getItemViewType(int index) {
        if (data.get(index) == null)
            return TYPE_FOOTER;
        return TYPE_MESSAGE;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_MESSAGE) {
            final MessageHolder holder = new MessageHolder(parent, settings);
            holder.buttons[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    if (position != NO_POSITION) {
                        DirectMessage message = data.get(position);
                        if (message != null) {
                            itemClickListener.onClick(message, OnMessageClickListener.Action.ANSWER);
                        }
                    }
                }
            });
            holder.buttons[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    if (position != NO_POSITION) {
                        DirectMessage message = data.get(position);
                        if (message != null) {
                            itemClickListener.onClick(message, OnMessageClickListener.Action.DELETE);
                        }
                    }
                }
            });
            holder.profile_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    if (position != NO_POSITION) {
                        DirectMessage message = data.get(position);
                        if (message != null) {
                            itemClickListener.onClick(message, OnMessageClickListener.Action.PROFILE);
                        }
                    }
                }
            });
            holder.mediaButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    if (position != NO_POSITION) {
                        DirectMessage message = data.get(position);
                        if (message != null) {
                            itemClickListener.onClick(message, OnMessageClickListener.Action.MEDIA);
                        }
                    }
                }
            });
            return holder;
        } else {
            final Footer footer = new Footer(parent, settings, false);
            footer.loadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = footer.getLayoutPosition();
                    if (position != NO_POSITION) {
                        boolean success = itemClickListener.onFooterClick(data.getNextCursor());
                        if (success) {
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
    public void onBindViewHolder(@NonNull ViewHolder vh, int index) {
        if (vh instanceof MessageHolder) {
            DirectMessage message = data.get(index);
            if (message != null) {
                User sender = message.getSender();
                Spanned text = Tagger.makeTextWithLinks(message.getText(), settings.getHighlightColor(), itemClickListener);

                MessageHolder holder = (MessageHolder) vh;
                holder.textViews[0].setText(sender.getUsername());
                holder.textViews[1].setText(sender.getScreenname());
                holder.textViews[2].setText(message.getReceiver().getScreenname());
                holder.textViews[3].setText(formatCreationTime(resources, message.getTimestamp()));
                holder.textViews[4].setText(text);
                if (sender.isVerified()) {
                    holder.verifiedIcon.setVisibility(VISIBLE);
                } else {
                    holder.verifiedIcon.setVisibility(GONE);
                }
                if (sender.isProtected()) {
                    holder.lockedIcon.setVisibility(VISIBLE);
                } else {
                    holder.lockedIcon.setVisibility(GONE);
                }
                if (message.getMedia() != null) {
                    holder.mediaButton.setVisibility(VISIBLE);
                } else {
                    holder.mediaButton.setVisibility(GONE);
                }
                if (settings.imagesEnabled() && !sender.getImageUrl().isEmpty()) {
                    String profileImageUrl = sender.getImageUrl();
                    if (!sender.hasDefaultProfileImage())
                        profileImageUrl += settings.getImageSuffix();
                    picasso.load(profileImageUrl).transform(new RoundedCornersTransformation(2, 0))
                            .error(R.drawable.no_image).into(holder.profile_img);
                } else {
                    holder.profile_img.setImageResource(0);
                }
            }
        } else if (vh instanceof Footer) {
            Footer footer = (Footer) vh;
            footer.setLoading(loadingIndex != NO_LOADING);
        }
    }

    /**
     * disable footer loading animation
     */
    public void disableLoading() {
        if (loadingIndex != NO_LOADING) {
            int oldIndex = loadingIndex;
            loadingIndex = NO_LOADING;
            notifyItemChanged(oldIndex);
        }
    }

    /**
     * listener for directmessage items
     */
    public interface OnMessageClickListener extends OnTagClickListener {

        /**
         * Actions performed by clicking the buttons
         */
        enum Action {
            ANSWER,
            DELETE,
            PROFILE,
            MEDIA
        }

        /**
         * called when a button was clicked
         *
         * @param message Message information
         * @param action  what button was clicked
         */
        void onClick(DirectMessage message, Action action);

        /**
         * called when the footer was clicked
         *
         * @param cursor message cursor
         * @return true if task was started
         */
        boolean onFooterClick(String cursor);
    }
}
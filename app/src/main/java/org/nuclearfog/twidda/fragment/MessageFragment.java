package org.nuclearfog.twidda.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.activity.MessagePopup;
import org.nuclearfog.twidda.activity.SearchPage;
import org.nuclearfog.twidda.activity.TweetDetail;
import org.nuclearfog.twidda.activity.UserProfile;
import org.nuclearfog.twidda.adapter.MessageAdapter;
import org.nuclearfog.twidda.adapter.MessageAdapter.OnItemSelected;
import org.nuclearfog.twidda.backend.MessageListLoader;
import org.nuclearfog.twidda.backend.TrendListLoader;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.helper.ErrorHandler;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.List;

import static android.os.AsyncTask.Status.FINISHED;
import static android.os.AsyncTask.Status.RUNNING;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.MessagePopup.KEY_DM_PREFIX;
import static org.nuclearfog.twidda.activity.SearchPage.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.activity.TweetDetail.KEY_TWEET_ID;
import static org.nuclearfog.twidda.activity.TweetDetail.KEY_TWEET_NAME;
import static org.nuclearfog.twidda.activity.TweetDetail.LINK_PATTERN;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_ID;

public class MessageFragment extends Fragment implements OnRefreshListener, OnItemSelected {

    private MessageListLoader messageTask;
    private SwipeRefreshLayout reload;
    private MessageAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle param) {
        Context context = inflater.getContext();

        GlobalSettings settings = GlobalSettings.getInstance(context);
        adapter = new MessageAdapter(this, settings);

        RecyclerView list = new RecyclerView(context);
        list.setLayoutManager(new LinearLayoutManager(context));
        list.setHasFixedSize(true);
        list.setAdapter(adapter);

        reload = new SwipeRefreshLayout(context);
        reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        reload.setOnRefreshListener(this);
        reload.addView(list);
        return reload;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (messageTask == null) {
            load(MessageListLoader.Action.DB);
        }
    }


    @Override
    public void onDestroy() {
        if (messageTask != null && messageTask.getStatus() == RUNNING)
            messageTask.cancel(true);
        super.onDestroy();
    }


    @Override
    public void onRefresh() {
        if (messageTask != null && messageTask.getStatus() != RUNNING) {
            load(MessageListLoader.Action.LOAD);
        }
    }


    @Override
    public void onTagClick(String tag) {
        if (getContext() != null && !reload.isRefreshing()) {
            Intent intent = new Intent(getContext(), SearchPage.class);
            intent.putExtra(KEY_SEARCH_QUERY, tag);
            startActivity(intent);
        }
    }


    @Override
    public void onLinkClick(String tag) {
        if (getContext() != null) {
            String shortLink = tag;
            int cut = shortLink.indexOf('?');
            if (cut > 0) {
                shortLink = shortLink.substring(0, cut);
            }
            if (LINK_PATTERN.matcher(shortLink).matches()) {
                String name = shortLink.substring(20, shortLink.indexOf('/', 20));
                long id = Long.parseLong(shortLink.substring(shortLink.lastIndexOf('/') + 1));
                Intent intent = new Intent(getContext(), TweetDetail.class);
                intent.putExtra(KEY_TWEET_ID, id);
                intent.putExtra(KEY_TWEET_NAME, name);
                startActivity(intent);
            } else {
                Uri link = Uri.parse(tag);
                Intent intent = new Intent(Intent.ACTION_VIEW, link);
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), R.string.error_connection_failed, LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    public void onClick(final Message message, Action action) {
        if (getContext() != null && !reload.isRefreshing()) {
            switch (action) {
                case ANSWER:
                    Intent sendDm = new Intent(getContext(), MessagePopup.class);
                    sendDm.putExtra(KEY_DM_PREFIX, message.getSender().getScreenname());
                    startActivity(sendDm);
                    break;

                case DELETE:
                    Builder confirmDialog = new Builder(getContext(), R.style.ConfirmDialog);
                    confirmDialog.setMessage(R.string.confirm_delete_message);
                    confirmDialog.setNegativeButton(R.string.confirm_no, null);
                    confirmDialog.setPositiveButton(R.string.confirm_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            messageTask = new MessageListLoader(MessageFragment.this, MessageListLoader.Action.DEL);
                            messageTask.execute(message.getId());
                        }
                    });
                    confirmDialog.show();
                    break;

                case PROFILE:
                    Intent profile = new Intent(getContext(), UserProfile.class);
                    profile.putExtra(KEY_PROFILE_ID, message.getSender().getId());
                    startActivity(profile);
                    break;
            }
        }
    }

    /**
     * set data to list
     *
     * @param data list of direct messages
     */
    public void setData(List<Message> data) {
        adapter.replaceAll(data);
    }

    /**
     * remove item from list
     *
     * @param id ID of the item
     */
    public void removeItem(long id) {
        adapter.remove(id);
    }

    /**
     * called from {@link TrendListLoader} to enable or disable RefreshLayout
     * @param enable true to enable RefreshLayout with delay
     */
    public void setRefresh(boolean enable) {
        if (enable) {
            reload.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (messageTask.getStatus() != FINISHED && !reload.isRefreshing())
                        reload.setRefreshing(true);
                }
            }, 500);
        } else {
            reload.setRefreshing(false);
        }
    }

    /**
     * called from {@link MessageListLoader} if an error occurs
     *
     * @param error Twitter exception
     */
    public void onError(EngineException error) {
        if (getContext() != null) {
            ErrorHandler.handleFailure(getContext(), error);
        }
    }


    private void load(MessageListLoader.Action m) {
        messageTask = new MessageListLoader(this, m);
        messageTask.execute();
    }
}
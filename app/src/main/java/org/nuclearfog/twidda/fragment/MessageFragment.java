package org.nuclearfog.twidda.fragment;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.activity.MessageEditor;
import org.nuclearfog.twidda.activity.SearchPage;
import org.nuclearfog.twidda.activity.TweetActivity;
import org.nuclearfog.twidda.activity.UserProfile;
import org.nuclearfog.twidda.adapter.MessageAdapter;
import org.nuclearfog.twidda.adapter.MessageAdapter.OnItemSelected;
import org.nuclearfog.twidda.backend.MessageLoader;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.lists.MessageList;
import org.nuclearfog.twidda.backend.model.Message;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.dialog.ConfirmDialog;
import org.nuclearfog.twidda.dialog.ConfirmDialog.DialogType;
import org.nuclearfog.twidda.dialog.ConfirmDialog.OnConfirmListener;

import static android.os.AsyncTask.Status.RUNNING;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.MessageEditor.KEY_DM_PREFIX;
import static org.nuclearfog.twidda.activity.SearchPage.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.activity.TweetActivity.KEY_TWEET_ID;
import static org.nuclearfog.twidda.activity.TweetActivity.KEY_TWEET_NAME;
import static org.nuclearfog.twidda.activity.TweetActivity.LINK_PATTERN;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_DATA;

/**
 * Fragment class for direct message lists
 *
 * @author nuclearfog
 */
public class MessageFragment extends ListFragment implements OnItemSelected, OnConfirmListener {

    private MessageLoader messageTask;
    private MessageAdapter adapter;
    private Dialog deleteDialog;

    private long deleteId;


    @Override
    protected void onCreate() {
        deleteDialog = new ConfirmDialog(requireContext(), DialogType.MESSAGE_DELETE, this);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (messageTask == null) {
            load(MessageLoader.Action.DB, null);
            setRefresh(true);
        }
    }


    @Override
    protected void onReset() {
        load(MessageLoader.Action.DB, null);
        setRefresh(true);
    }


    @Override
    public void onDestroy() {
        if (messageTask != null && messageTask.getStatus() == RUNNING)
            messageTask.cancel(true);
        super.onDestroy();
    }


    @Override
    protected void onReload() {
        if (messageTask != null && messageTask.getStatus() != RUNNING) {
            load(MessageLoader.Action.LOAD, null);
        }
    }


    @Override
    public void onTagClick(String tag) {
        if (!isRefreshing()) {
            Intent intent = new Intent(requireContext(), SearchPage.class);
            intent.putExtra(KEY_SEARCH_QUERY, tag);
            startActivity(intent);
        }
    }


    @Override
    public void onLinkClick(final String tag) {
        String shortLink = tag;
        int cut = shortLink.indexOf('?');
        if (cut > 0) {
            shortLink = shortLink.substring(0, cut);
        }
        if (LINK_PATTERN.matcher(shortLink).matches()) {
            String name = shortLink.substring(20, shortLink.indexOf('/', 20));
            long id = Long.parseLong(shortLink.substring(shortLink.lastIndexOf('/') + 1));
            Intent intent = new Intent(requireContext(), TweetActivity.class);
            intent.putExtra(KEY_TWEET_ID, id);
            intent.putExtra(KEY_TWEET_NAME, name);
            startActivity(intent);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(tag));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException err) {
                Toast.makeText(requireContext(), R.string.error_connection_failed, LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onClick(Message message, Action action) {
        if (!isRefreshing()) {
            switch (action) {
                case ANSWER:
                    Intent sendDm = new Intent(requireContext(), MessageEditor.class);
                    sendDm.putExtra(KEY_DM_PREFIX, message.getSender().getScreenname());
                    startActivity(sendDm);
                    break;

                case DELETE:
                    if (!deleteDialog.isShowing()) {
                        deleteDialog.show();
                    }
                    deleteId = message.getId();
                    break;

                case PROFILE:
                    Intent profile = new Intent(requireContext(), UserProfile.class);
                    profile.putExtra(KEY_PROFILE_DATA, message.getSender());
                    startActivity(profile);
                    break;
            }
        }
    }


    @Override
    public boolean onFooterClick(String cursor) {
        if (messageTask != null && messageTask.getStatus() != RUNNING) {
            load(MessageLoader.Action.LOAD, cursor);
            return true;
        }
        return false;
    }


    @Override
    public void onConfirm(DialogType type) {
        if (type == DialogType.MESSAGE_DELETE) {
            messageTask = new MessageLoader(this, MessageLoader.Action.DEL, null);
            messageTask.execute(deleteId);
        }
    }

    /**
     * set data to list
     *
     * @param data list of direct messages
     */
    public void setData(MessageList data) {
        adapter.setData(data);
        setRefresh(false);
    }

    /**
     * remove item from list
     *
     * @param id ID of the item
     */
    public void removeItem(long id) {
        adapter.remove(id);
    }


    @Override
    protected MessageAdapter initAdapter() {
        adapter = new MessageAdapter(settings, this);
        return adapter;
    }

    /**
     * called from {@link MessageLoader} if an error occurs
     *
     * @param error Twitter exception
     */
    public void onError(@NonNull EngineException error) {
        ErrorHandler.handleFailure(requireContext(), error);
        setRefresh(false);
    }

    /**
     * load content into the list
     *
     * @param action mode for loading or removing messages
     */
    private void load(MessageLoader.Action action, String cursor) {
        messageTask = new MessageLoader(this, action, cursor);
        messageTask.execute();
    }
}
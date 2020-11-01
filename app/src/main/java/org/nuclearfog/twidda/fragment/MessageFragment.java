package org.nuclearfog.twidda.fragment;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.activity.MessagePopup;
import org.nuclearfog.twidda.activity.SearchPage;
import org.nuclearfog.twidda.activity.TweetActivity;
import org.nuclearfog.twidda.activity.UserProfile;
import org.nuclearfog.twidda.adapter.MessageAdapter;
import org.nuclearfog.twidda.adapter.MessageAdapter.OnItemSelected;
import org.nuclearfog.twidda.backend.MessageListLoader;
import org.nuclearfog.twidda.backend.engine.EngineException;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.backend.utils.DialogBuilder;
import org.nuclearfog.twidda.backend.utils.DialogBuilder.OnDialogClick;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.database.GlobalSettings;

import java.util.List;

import static android.os.AsyncTask.Status.RUNNING;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.activity.MessagePopup.KEY_DM_PREFIX;
import static org.nuclearfog.twidda.activity.SearchPage.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.activity.TweetActivity.KEY_TWEET_ID;
import static org.nuclearfog.twidda.activity.TweetActivity.KEY_TWEET_NAME;
import static org.nuclearfog.twidda.activity.TweetActivity.LINK_PATTERN;
import static org.nuclearfog.twidda.activity.UserProfile.KEY_PROFILE_ID;
import static org.nuclearfog.twidda.backend.utils.DialogBuilder.DialogType.DEL_MESSAGE;

/**
 * Fragment class for direct message lists
 */
public class MessageFragment extends ListFragment implements OnItemSelected, OnDialogClick {

    private GlobalSettings settings;
    private MessageListLoader messageTask;
    private MessageAdapter adapter;
    private Dialog deleteDialog;

    private long deleteId;


    @Override
    protected void onCreate() {
        settings = GlobalSettings.getInstance(requireContext());
        deleteDialog = DialogBuilder.create(requireContext(), DEL_MESSAGE, this);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (messageTask == null) {
            load(MessageListLoader.Action.DB);
            setRefresh(true);
        }
    }


    @Override
    protected void onReset() {
        setRefresh(true);
        load(MessageListLoader.Action.DB);
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
            load(MessageListLoader.Action.LOAD);
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
    public void onLinkClick(String tag) {
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
            Uri link = Uri.parse(tag);
            Intent intent = new Intent(Intent.ACTION_VIEW, link);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException err) {
                Toast.makeText(requireContext(), R.string.error_connection_failed, LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onClick(final Message message, Action action) {
        if (!isRefreshing()) {
            switch (action) {
                case ANSWER:
                    Intent sendDm = new Intent(requireContext(), MessagePopup.class);
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
                    profile.putExtra(KEY_PROFILE_ID, message.getSender().getId());
                    startActivity(profile);
                    break;
            }
        }
    }


    @Override
    public void onConfirm(DialogBuilder.DialogType type) {
        messageTask = new MessageListLoader(this, MessageListLoader.Action.DEL);
        messageTask.execute(deleteId);
    }

    /**
     * set data to list
     *
     * @param data list of direct messages
     */
    public void setData(List<Message> data) {
        adapter.replaceAll(data);
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
        adapter = new MessageAdapter(this, settings);
        return adapter;
    }

    /**
     * called from {@link MessageListLoader} if an error occurs
     *
     * @param error Twitter exception
     */
    public void onError(EngineException error) {
        if (error != null)
            ErrorHandler.handleFailure(requireContext(), error);
        setRefresh(false);
    }

    /**
     * load content into the list
     *
     * @param action mode for loading or removing messages
     */
    private void load(MessageListLoader.Action action) {
        messageTask = new MessageListLoader(this, action);
        messageTask.execute();
    }
}
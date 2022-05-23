package org.nuclearfog.twidda.ui.fragments;

import static android.os.AsyncTask.Status.RUNNING;
import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.ui.activities.MessageEditor.KEY_DM_PREFIX;
import static org.nuclearfog.twidda.ui.activities.SearchPage.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.ui.activities.TweetActivity.KEY_TWEET_ID;
import static org.nuclearfog.twidda.ui.activities.TweetActivity.KEY_TWEET_NAME;
import static org.nuclearfog.twidda.ui.activities.TweetActivity.LINK_PATTERN;
import static org.nuclearfog.twidda.ui.activities.UserProfile.KEY_PROFILE_DATA;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.MessageAdapter;
import org.nuclearfog.twidda.adapter.MessageAdapter.OnMessageClickListener;
import org.nuclearfog.twidda.backend.async.MessageLoader;
import org.nuclearfog.twidda.backend.lists.Directmessages;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.model.DirectMessage;
import org.nuclearfog.twidda.ui.activities.ImageViewer;
import org.nuclearfog.twidda.ui.activities.MessageEditor;
import org.nuclearfog.twidda.ui.activities.SearchPage;
import org.nuclearfog.twidda.ui.activities.TweetActivity;
import org.nuclearfog.twidda.ui.activities.UserProfile;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.DialogType;
import org.nuclearfog.twidda.ui.dialogs.ConfirmDialog.OnConfirmListener;

/**
 * Fragment class for direct message lists
 *
 * @author nuclearfog
 */
public class MessageFragment extends ListFragment implements OnMessageClickListener, OnConfirmListener {

    private MessageLoader messageTask;
    private MessageAdapter adapter;
    private ConfirmDialog confirmDialog;

    private long deleteId;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        confirmDialog = new ConfirmDialog(requireContext());
        adapter = new MessageAdapter(requireContext(), this);
        setAdapter(adapter);

        confirmDialog.setConfirmListener(this);
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
    public void onClick(DirectMessage message, Action action) {
        if (!isRefreshing()) {
            switch (action) {
                case ANSWER:
                    Intent sendDm = new Intent(requireContext(), MessageEditor.class);
                    sendDm.putExtra(KEY_DM_PREFIX, message.getSender().getScreenname());
                    startActivity(sendDm);
                    break;

                case DELETE:
                    if (!confirmDialog.isShowing() && messageTask != null && messageTask.getStatus() != RUNNING) {
                        deleteId = message.getId();
                        confirmDialog.show(DialogType.MESSAGE_DELETE);
                    }
                    break;

                case PROFILE:
                    Intent profile = new Intent(requireContext(), UserProfile.class);
                    profile.putExtra(KEY_PROFILE_DATA, message.getSender());
                    startActivity(profile);
                    break;

                case MEDIA:
                    if (message.getMedia() != null) {
                        Intent imageIntent = new Intent(requireContext(), ImageViewer.class);
                        imageIntent.putExtra(ImageViewer.IMAGE_URIS, new Uri[]{message.getMedia()});
                        imageIntent.putExtra(ImageViewer.IMAGE_DOWNLOAD, true);
                        startActivity(imageIntent);
                    }
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
    public void onConfirm(DialogType type, boolean rememberChoice) {
        if (type == DialogType.MESSAGE_DELETE) {
            if (messageTask != null && messageTask.getStatus() != RUNNING) {
                messageTask = new MessageLoader(this, MessageLoader.Action.DEL, null, deleteId);
                messageTask.execute();
            }
        }
    }

    /**
     * set data to list
     *
     * @param data list of direct messages
     */
    public void setData(Directmessages data) {
        adapter.setData(data);
        setRefresh(false);
    }

    /**
     * remove item from list
     *
     * @param id ID of the item
     */
    public void removeItem(long id) {
        Toast.makeText(requireContext(), R.string.info_dm_removed, LENGTH_SHORT).show();
        adapter.remove(id);
    }

    /**
     * called from {@link MessageLoader} if an error occurs
     *
     * @param error Twitter exception
     */
    public void onError(@NonNull ErrorHandler.TwitterError error) {
        ErrorHandler.handleFailure(requireContext(), error);
        setRefresh(false);
    }

    /**
     * load content into the list
     *
     * @param action mode for loading or removing messages
     */
    private void load(MessageLoader.Action action, String cursor) {
        messageTask = new MessageLoader(this, action, cursor, -1L);
        messageTask.execute();
    }
}
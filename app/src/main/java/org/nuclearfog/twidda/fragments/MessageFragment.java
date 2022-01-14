package org.nuclearfog.twidda.fragments;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.activities.MediaViewer;
import org.nuclearfog.twidda.activities.MessageEditor;
import org.nuclearfog.twidda.activities.SearchPage;
import org.nuclearfog.twidda.activities.TweetActivity;
import org.nuclearfog.twidda.activities.UserProfile;
import org.nuclearfog.twidda.adapter.MessageAdapter;
import org.nuclearfog.twidda.adapter.MessageAdapter.OnItemSelected;
import org.nuclearfog.twidda.backend.MessageLoader;
import org.nuclearfog.twidda.backend.lists.Directmessages;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.dialog.ConfirmDialog;
import org.nuclearfog.twidda.dialog.ConfirmDialog.DialogType;
import org.nuclearfog.twidda.dialog.ConfirmDialog.OnConfirmListener;
import org.nuclearfog.twidda.model.DirectMessage;

import static android.os.AsyncTask.Status.*;
import static android.widget.Toast.*;
import static org.nuclearfog.twidda.activities.MediaViewer.*;
import static org.nuclearfog.twidda.activities.MessageEditor.*;
import static org.nuclearfog.twidda.activities.SearchPage.*;
import static org.nuclearfog.twidda.activities.TweetActivity.*;
import static org.nuclearfog.twidda.activities.UserProfile.*;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        deleteDialog = new ConfirmDialog(requireContext(), DialogType.MESSAGE_DELETE, this);
        adapter = new MessageAdapter(requireContext(), this);
        setAdapter(adapter);
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
                    deleteId = message.getId();
                    if (!deleteDialog.isShowing()) {
                        deleteDialog.show();
                    }
                    break;

                case PROFILE:
                    Intent profile = new Intent(requireContext(), UserProfile.class);
                    profile.putExtra(KEY_PROFILE_DATA, message.getSender());
                    startActivity(profile);
                    break;

                case MEDIA:
                    if (message.getMedia() != null) {
                        Intent mediaIntent = new Intent(requireContext(), MediaViewer.class);
                        mediaIntent.putExtra(KEY_MEDIA_URI, message.getMedia());
                        mediaIntent.putExtra(KEY_MEDIA_TYPE, MEDIAVIEWER_IMAGE);
                        startActivity(mediaIntent);
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
        messageTask = new MessageLoader(this, action, cursor);
        messageTask.execute();
    }
}
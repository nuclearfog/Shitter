package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.UserAdapter;
import org.nuclearfog.twidda.backend.items.TwitterUser;
import org.nuclearfog.twidda.fragment.UserFragment;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * User list loader task
 */
public class UserLoader extends AsyncTask<Object, Void, List<TwitterUser>> {

    public enum Mode {
        FOLLOWS,
        FRIENDS,
        RETWEET,
        FAVORIT,
        SEARCH,
        SUBSCRIBER,
        LIST
    }

    @Nullable
    private TwitterEngine.EngineException twException;
    private Mode mode;
    private WeakReference<UserFragment> ui;
    private TwitterEngine mTwitter;
    private UserAdapter adapter;


    public UserLoader(UserFragment fragment, Mode mode) {
        ui = new WeakReference<>(fragment);
        mTwitter = TwitterEngine.getInstance(fragment.getContext());
        adapter = fragment.getAdapter();
        this.mode = mode;
    }


    @Override
    protected void onPreExecute() {
        if (ui.get() != null)
            ui.get().setRefresh(true);
    }


    @Override
    protected List<TwitterUser> doInBackground(Object[] param) {
        try {
            switch (mode) {
                case FOLLOWS:
                    return mTwitter.getFollower((long) param[0]);

                case FRIENDS:
                    return mTwitter.getFollowing((long) param[0]);

                case RETWEET:
                    return mTwitter.getRetweeter((long) param[0]);

                case FAVORIT:
                    return new LinkedList<>();  // TODO not jet implemented in Twitter4J

                case SEARCH:
                    return mTwitter.searchUsers((String) param[0]);

                case SUBSCRIBER:
                    return mTwitter.getListFollower((long) param[0]);

                case LIST:
                    return mTwitter.getListMember((long) param[0]);

            }
        } catch (TwitterEngine.EngineException twException) {
            this.twException = twException;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }


    @Override
    protected void onPostExecute(@Nullable List<TwitterUser> users) {
        if (ui.get() != null) {
            if (users != null) {
                adapter.replaceAll(users);
                if (mode == Mode.FAVORIT)
                    Toast.makeText(ui.get().getContext(), R.string.info_not_implemented, Toast.LENGTH_SHORT).show();
            } else if (twException != null)
                Toast.makeText(ui.get().getContext(), twException.getMessageResource(), LENGTH_SHORT).show();
            ui.get().setRefresh(false);
        }
    }


    @Override
    protected void onCancelled() {
        if (ui.get() != null)
            ui.get().setRefresh(false);
    }


    @Override
    protected void onCancelled(@Nullable List<TwitterUser> users) {
        if (ui.get() != null) {
            if (users != null)
                adapter.replaceAll(users);
            ui.get().setRefresh(false);
        }
    }
}
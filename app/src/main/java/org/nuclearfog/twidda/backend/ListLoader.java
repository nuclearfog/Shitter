package org.nuclearfog.twidda.backend;

import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.ListAdapter;
import org.nuclearfog.twidda.backend.items.TwitterList;
import org.nuclearfog.twidda.fragment.ListFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;


public class ListLoader extends AsyncTask<Long, Void, List<TwitterList>> {

    public enum Action {
        LOAD,
        FOLLOW,
    }

    @Nullable
    private TwitterEngine.EngineException twException;
    private WeakReference<ListFragment> ui;
    private TwitterEngine mTwitter;
    private ListAdapter adapter;
    private Action action;

    public ListLoader(ListFragment frag, Action action) {
        ui = new WeakReference<>(frag);
        mTwitter = TwitterEngine.getInstance(frag.getContext());
        adapter = frag.getAdapter();
        this.action = action;
    }

    @Override
    protected void onPreExecute() {
        if (ui.get() != null)
            ui.get().setRefresh(true);
    }

    @Override
    protected List<TwitterList> doInBackground(Long[] param) {
        try {
            switch (action) {
                case LOAD:
                    return mTwitter.getUserList(param[0]);

                case FOLLOW:
                    TwitterList list = mTwitter.followUserList(param[0]);
                    List<TwitterList> result = new ArrayList<>(1);
                    result.add(list);
                    return result;
            }

        } catch (TwitterEngine.EngineException twException) {
            this.twException = twException;
        }
        return null;
    }

    @Override
    protected void onPostExecute(@Nullable List<TwitterList> result) {
        if (ui.get() != null) {
            if (result != null) {
                switch (action) {
                    case LOAD:
                        adapter.setData(result);
                        break;

                    case FOLLOW:
                        TwitterList list = result.get(0);
                        adapter.updateItem(list);
                        if (list.isFollowing())
                            Toast.makeText(ui.get().getContext(), R.string.followed, LENGTH_SHORT).show();
                        else
                            Toast.makeText(ui.get().getContext(), R.string.info_unfollowed, LENGTH_SHORT).show();
                        break;
                }
            }
            if (twException != null)
                Toast.makeText(ui.get().getContext(), twException.getMessageResource(), LENGTH_SHORT).show();
            ui.get().setRefresh(false);
        }
    }

    @Override
    protected void onCancelled() {
        if (ui.get() != null)
            ui.get().setRefresh(false);
    }
}
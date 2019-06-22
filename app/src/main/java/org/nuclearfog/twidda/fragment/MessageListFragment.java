package org.nuclearfog.twidda.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.adapter.MessageAdapter;
import org.nuclearfog.twidda.adapter.MessageAdapter.OnItemSelected;
import org.nuclearfog.twidda.backend.items.Message;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.fragment.backend.MessageLoader;
import org.nuclearfog.twidda.fragment.backend.MessageLoader.Mode;
import org.nuclearfog.twidda.window.MessagePopup;
import org.nuclearfog.twidda.window.SearchPage;
import org.nuclearfog.twidda.window.UserProfile;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.window.MessagePopup.KEY_DM_ADDITION;
import static org.nuclearfog.twidda.window.SearchPage.KEY_SEARCH;
import static org.nuclearfog.twidda.window.UserProfile.KEY_PROFILE_ID;
import static org.nuclearfog.twidda.window.UserProfile.KEY_PROFILE_NAME;


public class MessageListFragment extends Fragment implements OnRefreshListener, OnItemSelected {

    private MessageLoader messageTask;
    private SwipeRefreshLayout reload;
    private View root;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        GlobalSettings settings = GlobalSettings.getInstance(getContext());
        View v = inflater.inflate(R.layout.fragment_list, parent, false);
        RecyclerView list = v.findViewById(R.id.fragment_list);
        reload = v.findViewById(R.id.fragment_reload);
        reload.setProgressBackgroundColorSchemeColor(settings.getHighlightColor());
        reload.setOnRefreshListener(this);

        MessageAdapter adapter = new MessageAdapter(this);
        adapter.setColor(settings.getFontColor(), settings.getHighlightColor());
        adapter.toggleImage(settings.getImageLoad());
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setHasFixedSize(true);
        list.setAdapter(adapter);

        return v;
    }


    @Override
    public void onViewCreated(@NonNull View v, Bundle param) {
        root = v;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (messageTask == null) {
            messageTask = new MessageLoader(root, Mode.DB);
            messageTask.execute();
        }
    }


    @Override
    public void onStop() {
        if (messageTask != null && messageTask.getStatus() == RUNNING)
            messageTask.cancel(true);
        super.onStop();
    }


    @Override
    public void onRefresh() {
        messageTask = new MessageLoader(root, Mode.LOAD);
        messageTask.execute();
    }


    @Override
    public void onClick(String tag) {
        if (reload != null && !reload.isRefreshing()) {
            Intent intent = new Intent(getContext(), SearchPage.class);
            intent.putExtra(KEY_SEARCH, tag);
            startActivity(intent);
        }
    }


    @Override
    public void onClick(Message message, Action action) {
        if (reload != null && !reload.isRefreshing()) {
            switch (action) {
                case ANSWER:
                    Intent sendDm = new Intent(getContext(), MessagePopup.class);
                    sendDm.putExtra(KEY_DM_ADDITION, message.getSender().getScreenname());
                    startActivity(sendDm);
                    break;

                case DELETE:
                    messageTask = new MessageLoader(root, Mode.DEL);
                    messageTask.execute(message.getId());
                    break;

                case PROFILE:
                    Intent profile = new Intent(getContext(), UserProfile.class);
                    profile.putExtra(KEY_PROFILE_ID, message.getSender().getId());
                    profile.putExtra(KEY_PROFILE_NAME, message.getSender().getScreenname());
                    startActivity(profile);
                    break;
            }
        }
    }
}
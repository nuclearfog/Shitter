package org.nuclearfog.twidda.fragment;

import android.content.Intent;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        adapter.setColor(settings.getHighlightColor(), settings.getFontColor());
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
        super.onStop();
        if (messageTask != null && messageTask.getStatus() == Status.RUNNING)
            messageTask.cancel(true);
    }


    @Override
    public void onRefresh() {
        messageTask = new MessageLoader(root, Mode.LOAD);
        messageTask.execute();
    }


    @Override
    public void onClick(String tag) {
        if (!reload.isRefreshing()) {
            Intent intent = new Intent(getContext(), SearchPage.class);
            intent.putExtra("search", tag);
            startActivity(intent);
        }
    }


    @Override
    public void onClick(Message message, Action action) {
        if (!reload.isRefreshing()) {
            switch (action) {
                case ANSWER:
                    Intent sendDm = new Intent(getContext(), MessagePopup.class);
                    sendDm.putExtra("username", message.getSender().getScreenname());
                    startActivity(sendDm);
                    break;
                case DELETE:
                    messageTask = new MessageLoader(root, Mode.DEL);
                    messageTask.execute(message.getId());
                    break;
                case PROFILE:
                    Intent profile = new Intent(getContext(), UserProfile.class);
                    profile.putExtra("userID", message.getSender().getId());
                    profile.putExtra("username", message.getSender().getScreenname());
                    startActivity(profile);
                    break;
            }
        }
    }
}
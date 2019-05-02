package org.nuclearfog.twidda.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.nuclearfog.twidda.adapter.MessageAdapter;
import org.nuclearfog.twidda.fragment.backend.MessageLoader;

import static android.os.AsyncTask.Status.RUNNING;
import static org.nuclearfog.twidda.fragment.backend.MessageLoader.Mode.LOAD;


public class MessageListFragment extends Fragment implements OnRefreshListener{

    private MessageLoader messageTask;
    private SwipeRefreshLayout root;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle param) {
        root = new SwipeRefreshLayout(inflater.getContext());
        RecyclerView list = new RecyclerView(inflater.getContext());
        root.setOnRefreshListener(this);
        root.addView(list);
        return root;
    }


    @Override
    public void onStart() {
        super.onStart();
        if(messageTask == null) {
            messageTask = new MessageLoader(root, LOAD);
            messageTask.execute();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if(messageTask != null && messageTask.getStatus() == RUNNING)
            messageTask.cancel(true);
    }


    @Override
    public void onRefresh() {
        messageTask = new MessageLoader(root, LOAD);
        messageTask.execute();
    }






}
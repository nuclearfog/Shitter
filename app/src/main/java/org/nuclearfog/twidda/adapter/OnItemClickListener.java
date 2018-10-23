package org.nuclearfog.twidda.adapter;

import android.support.v7.widget.RecyclerView;

public interface OnItemClickListener {

    /**
     * Item Click Listener
     *
     * @param rv    RecyclerView
     * @param index Position of View
     */
    void onItemClick(RecyclerView rv, int index);

}
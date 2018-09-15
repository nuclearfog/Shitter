package org.nuclearfog.twidda.viewadapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.nuclearfog.twidda.R;

public class WorldIdAdapter extends ArrayAdapter {

    private LayoutInflater mInflater;
    private String[] location;
    private int[] worldId;


    public WorldIdAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_dropdown_item);
        mInflater = LayoutInflater.from(context);
        Resources res = context.getResources();
        location = res.getStringArray(R.array.location);
        worldId = res.getIntArray(R.array.woeId);
    }


    @Override
    public int getCount() {
        return location.length;
    }


    @Override
    public String getItem(int position) {
        return location[position];
    }


    @Override
    public long getItemId(int position) {
        return worldId[position];
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = mInflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }
        TextView country = view.findViewById(android.R.id.text1);
        country.setText(getItem(position));
        return view;
    }
}
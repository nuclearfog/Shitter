package org.nuclearfog.twidda.ViewAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.nuclearfog.twidda.Backend.ImageDownloader;
import org.nuclearfog.twidda.DataBase.UserDatabase;
import org.nuclearfog.twidda.R;

public class UserAdapter extends ArrayAdapter {

    private Context context;
    private UserDatabase userDatabase;
    private ViewGroup p;

    public UserAdapter(Context context, UserDatabase userDatabase) {
        super(context, R.layout.user);
        this.context = context;
        this.userDatabase = userDatabase;
    }

    public UserDatabase getAdapter(){
        return userDatabase;
    }

    @Override
    public int getCount() {
        return userDatabase.getSize();
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        p = parent;
        if(v == null) {
            LayoutInflater inf=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.user, parent,false);
        }

        ((TextView)v.findViewById(R.id.username_detail)).setText(userDatabase.getUsername(position));
        ((TextView)v.findViewById(R.id.screenname_detail)).setText('@'+userDatabase.getScreenname(position));
        ImageView imgView = v.findViewById(R.id.user_profileimg);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView parent = ((ListView)p);
                int position = parent.getPositionForView(v);
                parent.performItemClick(v,position,0);
            }
        });

        if(userDatabase.loadImages()) {
            ImageDownloader imgDl = new ImageDownloader(imgView);
            imgDl.execute(userDatabase.getProfileURL(position));
        }

        return v;
    }
}
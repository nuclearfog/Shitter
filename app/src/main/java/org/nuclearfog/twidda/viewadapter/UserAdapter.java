package org.nuclearfog.twidda.viewadapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import org.nuclearfog.twidda.window.ColorPreferences;
import org.nuclearfog.twidda.database.UserDatabase;
import org.nuclearfog.twidda.R;

public class UserAdapter extends ArrayAdapter implements View.OnClickListener {

    private UserDatabase userDatabase;
    private ViewGroup p;
    private LayoutInflater inf;
    private int background;
    private Context context;

    public UserAdapter(Context context, UserDatabase userDatabase) {
        super(context, R.layout.useritem);
        this.userDatabase = userDatabase;
        this.context = context;
        inf = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ColorPreferences mColor = ColorPreferences.getInstance(context);
        background = mColor.getColor(ColorPreferences.BACKGROUND);
    }

    public UserDatabase getData(){
        return userDatabase;
    }

    @Override
    public int getCount() {
        return userDatabase.getSize();
    }

    @Override
    public long getItemId(int position){
        return userDatabase.getUserID(position);
    }

    @NonNull
    @Override
    public View getView(int position, View v, @NonNull ViewGroup parent) {
        p = parent;
        if(v == null) {
            v = inf.inflate(R.layout.useritem, parent,false);
            v.setBackgroundColor(background);
            v.setOnClickListener(this);
        }
        ((TextView)v.findViewById(R.id.username_detail)).setText(userDatabase.getUsername(position));
        ((TextView)v.findViewById(R.id.screenname_detail)).setText(userDatabase.getScreenname(position));
        ImageView pb = v.findViewById(R.id.user_profileimg);

        if(userDatabase.loadImages()) {
            Picasso.with(context).load(userDatabase.getImageUrl(position)).into(pb);
        }
        if(userDatabase.isVerified(position)) {
            v.findViewById(R.id.verified).setVisibility(View.VISIBLE);
        } else {
            v.findViewById(R.id.verified).setVisibility(View.INVISIBLE);
        }
        return v;
    }

    @Override
    public void onClick(View v) {
        ListView parent = ((ListView)p);
        int position = parent.getPositionForView(v);
        parent.performItemClick(v,position,0);
    }
}
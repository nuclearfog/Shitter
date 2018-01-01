package org.nuclearfog.twidda.Window;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.nuclearfog.twidda.Backend.ShowStatus;
import org.nuclearfog.twidda.DataBase.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.ViewAdapter.TimelineAdapter;

public class TweetDetail extends AppCompatActivity {

    private ListView answer_list;
    private Context context;
    private long tweetID;
    private long userID;

    @Override
    protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.tweet_detail);
        tweetID = getIntent().getExtras().getLong("tweetID");
        userID = getIntent().getExtras().getLong("userID");

        answer_list = (ListView) findViewById(R.id.answer_list);
        ImageView pb = (ImageView) findViewById(R.id.profileimage_detail);
        pb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent profile = new Intent(getApplicationContext(), UserProfile.class);
              Bundle bundle = new Bundle();
              bundle.putLong("userID",userID);
              profile.putExtras(bundle);
              startActivity(profile);
            }
        });
        setContent();
        setListViewListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setListViewListener() {
        context = getApplicationContext();
        answer_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TimelineAdapter tlAdp = (TimelineAdapter) answer_list.getAdapter();
                TweetDatabase twDB = tlAdp.getAdapter();
                long tweetID = twDB.getTweetId(position);
                long userID = twDB.getUserID(position);
                Intent intent = new Intent(context, TweetDetail.class);
                Bundle bundle = new Bundle();
                bundle.putLong("tweetID",tweetID);
                bundle.putLong("userID",userID);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void setContent() {
        ShowStatus set = new ShowStatus(this);
        set.execute(tweetID);
    }
}
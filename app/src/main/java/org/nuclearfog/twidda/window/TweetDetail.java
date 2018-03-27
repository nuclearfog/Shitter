package org.nuclearfog.twidda.window;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import static android.content.DialogInterface.*;

import org.nuclearfog.twidda.backend.StatusLoader;
import org.nuclearfog.twidda.backend.TwitterEngine;
import org.nuclearfog.twidda.backend.listitems.*;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;

/**
 * Detailed Tweet Window
 * @see StatusLoader
 */
public class TweetDetail extends AppCompatActivity implements View.OnClickListener,
        TimelineRecycler.OnItemClicked, DialogInterface.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView answer_list;
    private long tweetID;
    private long userID;
    private StatusLoader mStat, mReply;
    private String username = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.tweetpage);
        getExtras(getIntent().getExtras());

        boolean home = userID == TwitterEngine.getHomeId();

        answer_list = (RecyclerView) findViewById(R.id.answer_list);
        Button retweet = (Button) findViewById(R.id.rt_button_detail);
        Button favorite = (Button) findViewById(R.id.fav_button_detail);
        Button delete = (Button) findViewById(R.id.delete);
        SwipeRefreshLayout answerReload = (SwipeRefreshLayout) findViewById(R.id.answer_reload);
        TextView txtRt = (TextView) findViewById(R.id.no_rt_detail);
        TextView txtFav = (TextView) findViewById(R.id.no_fav_detail);
        TextView date = (TextView) findViewById(R.id.timedetail);
        answer_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        if(home) {
            delete.setVisibility(View.VISIBLE);
        }
        favorite.setOnClickListener(this);
        retweet.setOnClickListener(this);
        answerReload.setOnRefreshListener(this);
        txtFav.setOnClickListener(this);
        txtRt.setOnClickListener(this);
        date.setOnClickListener(this);
        delete.setOnClickListener(this);
        setContent();
    }

    @Override
    protected void onDestroy() {
        mStat.cancel(true);
        if(mReply != null)
            mReply.cancel(true);
        super.onDestroy();
    }

    /**
     * Home Button
     */
    @Override
    protected void onUserLeaveHint(){
        super.onUserLeaveHint();
        overridePendingTransition(0,0);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Bundle bundle = new Bundle();
        StatusLoader mStat = new StatusLoader(this);
        switch(v.getId()) {
            case R.id.rt_button_detail:
                mStat.execute(tweetID, StatusLoader.RETWEET);
                break;
            case R.id.fav_button_detail:
                mStat.execute(tweetID, StatusLoader.FAVORITE);
                break;
            case R.id.no_rt_detail:
                intent = new Intent(this, UserDetail.class);
                bundle.putLong("tweetID",tweetID);
                bundle.putLong("mode",2L);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.no_fav_detail:
                intent = new Intent(this, UserDetail.class);
                bundle.putLong("tweetID",tweetID);
                bundle.putLong("mode",3L);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.delete:
                AlertDialog.Builder alerta = new AlertDialog.Builder(this);
                alerta.setMessage("Tweet löschen?");
                alerta.setPositiveButton(R.string.yes_confirm, this);
                alerta.setNegativeButton(R.string.no_confirm, this);
                alerta.show();
                break;
            case R.id.timedetail:
                intent = new Intent(Intent.ACTION_VIEW);
                String tweetlink = "https://twitter.com/"+username+"/status/"+tweetID;
                intent.setData(Uri.parse(tweetlink));
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onClick(DialogInterface d, int id) {
        switch(id){
            case BUTTON_NEGATIVE:
                break;
            case BUTTON_POSITIVE:
                new StatusLoader(this).execute(tweetID, StatusLoader.DELETE);
                break;
        }
    }

    @Override
    public void onItemClick(View view, ViewGroup parent, int position) {
        TimelineRecycler tlAdp = (TimelineRecycler) answer_list.getAdapter();
        Tweet tweet = tlAdp.getData().get(position);
        long userID = tweet.user.userID;
        long tweetID = tweet.tweetID;
        String username = tweet.user.screenname;
        Intent intent = new Intent(getApplicationContext(), TweetDetail.class);
        Bundle bundle = new Bundle();
        bundle.putLong("userID",userID);
        bundle.putLong("tweetID",tweetID);
        bundle.putString("username", username);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        mReply = new StatusLoader(this);
        mReply.execute(tweetID, StatusLoader.LOAD_REPLY);
    }

    private void setContent() {
        ColorPreferences mColor = ColorPreferences.getInstance(getApplicationContext());
        int backgroundColor = mColor.getColor(ColorPreferences.BACKGROUND);
        int fontColor = mColor.getColor(ColorPreferences.FONT_COLOR);
        CollapsingToolbarLayout cLayout = (CollapsingToolbarLayout) findViewById(R.id.tweet_detail);
        LinearLayout tweetaction = (LinearLayout) findViewById(R.id.tweetbar);
        TextView txtTw = (TextView) findViewById(R.id.tweet_detailed);
        cLayout.setBackgroundColor(backgroundColor);
        tweetaction.setBackgroundColor(backgroundColor);
        answer_list.setBackgroundColor(backgroundColor);
        txtTw.setTextColor(fontColor);

        mStat = new StatusLoader(this);
        mReply = new StatusLoader(this);
        mStat.execute(tweetID, StatusLoader.LOAD_TWEET);
        mReply.execute(tweetID, StatusLoader.LOAD_REPLY);
    }

    @SuppressWarnings("ConstantConditions")
    private void getExtras(Bundle b) {
        tweetID = b.getLong("tweetID");
        userID = b.getLong("userID");
        username = b.getString("username");
    }
}
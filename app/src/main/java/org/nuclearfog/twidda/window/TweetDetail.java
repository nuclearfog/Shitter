package org.nuclearfog.twidda.window;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import static android.content.DialogInterface.*;

import org.nuclearfog.twidda.backend.StatusLoader;
import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineAdapter;

/**
 * Detailed Tweet Window
 * @see StatusLoader
 */
public class TweetDetail extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener, DialogInterface.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ListView answer_list;
    private long tweetID;
    private long userID;
    private StatusLoader mStat, mReply;
    private static String username;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.tweet_detail);
        getExtras(getIntent().getExtras());

        SharedPreferences settings = getSharedPreferences("settings", 0);
        boolean home = userID == settings.getLong("userID", -1);

        answer_list = (ListView) findViewById(R.id.answer_list);
        Button answer = (Button) findViewById(R.id.answer_button);
        Button retweet = (Button) findViewById(R.id.rt_button_detail);
        Button favorite = (Button) findViewById(R.id.fav_button_detail);
        Button delete = (Button) findViewById(R.id.delete);
        ImageView pb =(ImageView) findViewById(R.id.profileimage_detail);
        SwipeRefreshLayout answerReload = (SwipeRefreshLayout) findViewById(R.id.answer_reload);

        TextView txtRt = (TextView) findViewById(R.id.no_rt_detail);
        TextView txtFav = (TextView) findViewById(R.id.no_fav_detail);
        TextView date = (TextView) findViewById(R.id.timedetail);
        if(home) {
            delete.setVisibility(View.VISIBLE);
        }

        answer_list.setOnItemClickListener(this);
        favorite.setOnClickListener(this);
        retweet.setOnClickListener(this);
        answerReload.setOnRefreshListener(this);
        answer.setOnClickListener(this);
        txtFav.setOnClickListener(this);
        txtRt.setOnClickListener(this);
        date.setOnClickListener(this);
        delete.setOnClickListener(this);
        pb.setOnClickListener(this);
        setContent();
    }

    @Override
    protected void onDestroy() {
        mStat.cancel(true);
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
            case R.id.answer_button:
                intent = new Intent(getApplicationContext(), TweetPopup.class);
                bundle.putLong("TweetID", tweetID);
                bundle.putString("Addition", username);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.rt_button_detail:
                mStat.execute(tweetID, StatusLoader.RETWEET);
                break;
            case R.id.fav_button_detail:
                mStat.execute(tweetID, StatusLoader.FAVORITE);
                break;
            case R.id.no_rt_detail:
                intent = new Intent(getApplicationContext(), UserDetail.class);
                bundle.putLong("userID",userID);
                bundle.putLong("tweetID",tweetID);
                bundle.putLong("mode",2L);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.no_fav_detail:
                intent = new Intent(getApplicationContext(), UserDetail.class);
                bundle.putLong("userID",userID);
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
            case R.id.profileimage_detail:
                intent = new Intent(getApplicationContext(), UserProfile.class);
                Bundle b = new Bundle();
                b.putLong("userID",userID);
                b.putString("username", username);
                intent.putExtras(b);
                startActivity(intent);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TimelineAdapter tlAdp = (TimelineAdapter) answer_list.getAdapter();
        TweetDatabase twDB = tlAdp.getData();
        long userID = twDB.getUserID(position);
        long tweetID = twDB.getTweetId(position);
        Intent intent = new Intent(getApplicationContext(), TweetDetail.class);
        Bundle bundle = new Bundle();
        bundle.putLong("userID",userID);
        bundle.putLong("tweetID",tweetID);
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
package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.TwitterException;
import twitter4j.User;

import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.window.ColorPreferences;
import org.nuclearfog.twidda.window.TweetDetail;

public class StatusLoader extends AsyncTask<Long, Void, Long> {

    private static final long ERROR = -1;
    public static final long RETWEET = 0;
    public static final long FAVORITE = 1;
    public static final long DELETE = 2;
    public static final long LOAD_TWEET = 3;
    public static final long LOAD_REPLY = 4;

    private TwitterEngine mTwitter;
    private List<twitter4j.Status> answers;
    private TimelineRecycler tlAdp;
    private RecyclerView replyList;
    private Bitmap profile_btm;
    private String usernameStr, scrNameStr, tweetStr, dateString;
    private String repliedUsername, apiName, retweeter;
    private String medialinks[];
    private String errMSG = "";
    private boolean retweeted, favorited, toggleImg, verified;
    private boolean rtFlag = false;
    private long userReply, tweetReplyID;
    private int rt, fav;
    private int highlight;

    private WeakReference<TweetDetail> ui;

    public StatusLoader(Context c) {
        mTwitter = TwitterEngine.getInstance(c);
        answers = new ArrayList<>();
        SharedPreferences settings = c.getSharedPreferences("settings", 0);
        toggleImg = settings.getBoolean("image_load", true);
        highlight = ColorPreferences.getInstance(c).getColor(ColorPreferences.HIGHLIGHTING);

        ui = new WeakReference<>((TweetDetail)c);
        replyList = (RecyclerView) ui.get().findViewById(R.id.answer_list);
    }


    /**
     * @param data [0] TWEET ID , [1] Mode
     */
    @Override
    protected Long doInBackground(Long... data) {
        long tweetID = data[0];
        long mode = data[1];
        try {
            twitter4j.Status currentTweet = mTwitter.getStatus(tweetID);
            twitter4j.Status embeddedTweet = currentTweet.getRetweetedStatus();
            if(embeddedTweet != null) {
                retweeter = "Retweet @"+currentTweet.getUser().getScreenName();
                currentTweet = mTwitter.getStatus(embeddedTweet.getId());
                tweetID = currentTweet.getId();
                rtFlag = true;
            }
            rt = currentTweet.getRetweetCount();
            fav = currentTweet.getFavoriteCount();
            retweeted = currentTweet.isRetweetedByMe();
            favorited = currentTweet.isFavorited();

            User user = currentTweet.getUser();

            if(mode == LOAD_TWEET) {
                userReply = currentTweet.getInReplyToUserId();
                tweetReplyID = currentTweet.getInReplyToStatusId();
                tweetStr = currentTweet.getText();
                verified = user.isVerified();
                usernameStr = user.getName();
                scrNameStr = '@'+user.getScreenName();
                apiName = formatString(currentTweet.getSource());
                dateString = DateFormat.getDateTimeInstance().format(currentTweet.getCreatedAt());

                if(userReply > 0)
                    repliedUsername = currentTweet.getInReplyToScreenName();
                if(toggleImg) {
                    String pbLink = user.getProfileImageURL();
                    InputStream iStream = new URL(pbLink).openStream();
                    profile_btm = BitmapFactory.decodeStream(iStream);

                    MediaEntity[] media = currentTweet.getMediaEntities();
                    medialinks = new String[media.length];
                    for(int i = 0 ; i < media.length ; i++) {
                        medialinks[i] = media[i].getMediaURL();
                    }
                }
            }
            else if(mode == RETWEET) {
                if(retweeted) {
                    mTwitter.retweet(tweetID, true);
                    TweetDatabase.delete(ui.get(), tweetID);
                    retweeted = false;
                    rt--;
                } else {
                    mTwitter.retweet(tweetID, false);
                    retweeted = true;
                    rt++;
                }
            }
            else if(mode == FAVORITE) {
                if(favorited) {
                    mTwitter.favorite(tweetID, true);
                    favorited = false;
                    fav--;
                } else {
                    mTwitter.favorite(tweetID, false);
                    favorited = true;
                    fav++;
                }
            }
            else if(mode == LOAD_REPLY) {
                String replyname = user.getScreenName();
                tlAdp = (TimelineRecycler) replyList.getAdapter();
                if(tlAdp != null && tlAdp.getItemCount() > 0)
                    tweetID = tlAdp.getItemId(0);
                answers = mTwitter.getAnswers(replyname, tweetID);
            }
            else if(mode == DELETE) {
                mTwitter.deleteTweet(tweetID);
                TweetDatabase.delete(ui.get(),tweetID);
            }
        }catch(TwitterException e) {
            int err = e.getErrorCode();
            if(err == 144) { // gelöscht
                TweetDatabase.delete(ui.get(),tweetID);
                errMSG = e.getMessage();
            }
            e.printStackTrace();
            return ERROR;
        } catch(Exception err) {
            errMSG = err.getMessage();
            return ERROR;
        }
        return mode;
    }

    @Override
    protected void onPostExecute(Long mode) {

        TweetDetail connect = ui.get();
        if(connect == null)
            return;

        final Context c = connect.getApplicationContext();

        TextView tweet = (TextView)connect.findViewById(R.id.tweet_detailed);
        TextView username = (TextView)connect.findViewById(R.id.usernamedetail);
        TextView scrName = (TextView)connect.findViewById(R.id.scrnamedetail);
        TextView date = (TextView)connect.findViewById(R.id.timedetail);
        TextView replyName = (TextView)connect.findViewById(R.id.answer_reference_detail);
        TextView txtAns = (TextView)connect.findViewById(R.id.no_ans_detail);
        TextView txtRet = (TextView)connect.findViewById(R.id.no_rt_detail);
        TextView txtFav = (TextView)connect.findViewById(R.id.no_fav_detail);
        TextView used_api = (TextView)connect.findViewById(R.id.used_api);
        TextView userRetweet = (TextView)connect.findViewById(R.id.rt_info);
        SwipeRefreshLayout ansReload = (SwipeRefreshLayout)connect.findViewById(R.id.answer_reload);
        ImageView profile_img = (ImageView)connect.findViewById(R.id.profileimage_detail);
        ImageView tweet_verify =(ImageView)connect.findViewById(R.id.tweet_verify);
        Button retweetButton = (Button)connect.findViewById(R.id.rt_button_detail);
        Button favoriteButton = (Button)connect.findViewById(R.id.fav_button_detail);
        Button mediabutton = (Button)connect.findViewById(R.id.image_attach);

        if(mode == LOAD_TWEET) {
            tweet.setText(highlight(tweetStr));
            username.setText(usernameStr);
            scrName.setText(scrNameStr);
            date.setText(dateString);
            used_api.setText(apiName);

            String favStr = Integer.toString(fav);
            String rtStr = Integer.toString(rt);
            txtFav.setText(favStr);
            txtRet.setText(rtStr);

            if(repliedUsername != null) {
                String reply = "antwort @"+repliedUsername;
                replyName.setText(reply);
                replyName.setVisibility(View.VISIBLE);
            }
            if(rtFlag) {
                userRetweet.setText(retweeter);
            }
            if(verified) {
                tweet_verify.setVisibility(View.VISIBLE);
            }
            if(toggleImg) {
                profile_img.setImageBitmap(profile_btm);
                if(medialinks.length != 0) {
                    mediabutton.setVisibility(View.VISIBLE);
                    mediabutton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new ImagePopup(c).execute(medialinks);
                        }
                    });
                }
            }
            setIcons(favoriteButton, retweetButton);
            replyName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(c, TweetDetail.class);
                    intent.putExtra("tweetID",tweetReplyID);
                    intent.putExtra("userID",userReply);
                    intent.putExtra("username", repliedUsername);
                    c.startActivity(intent);
                }
            });
        }
        else if(mode == RETWEET) {
            String rtStr = Integer.toString(rt);
            txtRet.setText(rtStr);
            setIcons(favoriteButton, retweetButton);
        }
        else if(mode == FAVORITE) {
            String favStr = Integer.toString(fav);
            txtFav.setText(favStr);
            setIcons(favoriteButton, retweetButton);
        }
        else if(mode == LOAD_REPLY) {
            if(tlAdp == null || tlAdp.getItemCount() == 0) {
                TweetDatabase tweetDatabase = new TweetDatabase(answers,c);
                tlAdp = new TimelineRecycler(tweetDatabase,(ui.get()));
                replyList.setAdapter(tlAdp);
            } else {
                TweetDatabase twDb = tlAdp.getData();
                twDb.insert(answers,false);
                tlAdp.notifyDataSetChanged();
            }
            ansReload.setRefreshing(false);
            String ansStr = Integer.toString(tlAdp.getItemCount());
            txtAns.setText(ansStr);
        }
        else if(mode == DELETE) {
            Toast.makeText(c, "Tweet gelöscht", Toast.LENGTH_LONG).show();
            ((TweetDetail)c).finish();
        }
        else {
            Toast.makeText(c, "Fehler beim Laden: "+errMSG, Toast.LENGTH_LONG).show();
            if(ansReload.isRefreshing()) {
                ansReload.setRefreshing(false);
            }
        }
    }

    private String formatString(String input) {
        StringBuilder output = new StringBuilder("gesendet von: ");
        boolean openTag = false;
        for(int i = 0 ; i < input.length() ; i++){
            char current = input.charAt(i);
            if(current == '>' && !openTag){
                openTag = true;
            } else if(current == '<'){
                openTag = false;
            } else if(openTag) {
                output.append(current);
            }
        }
        return output.toString();
    }

    private SpannableStringBuilder highlight(String tweet) {
        SpannableStringBuilder sTweet = new SpannableStringBuilder(tweet);
        int start = 0;
        boolean marked = false;
        for(int i = 0 ; i < tweet.length() ; i++) {
            char current = tweet.charAt(i);
            switch(current){
                case '@':
                case '#':
                    start = i;
                    marked = true;
                    break;
                case '\'':
                case '\"':
                case '\n':
                case ')':
                case '(':
                case ':':
                case ' ':
                case '.':
                case ',':
                case '!':
                case '?':
                case '-':
                    if(marked) {
                        sTweet.setSpan(new ForegroundColorSpan(highlight),start,i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    marked = false;
                    break;
            }
        }
        if(marked) {
            sTweet.setSpan(new ForegroundColorSpan(highlight),start,tweet.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return sTweet;
    }

    private void setIcons(Button favoriteButton, Button retweetButton) {
        if(favorited)
            favoriteButton.setBackgroundResource(R.drawable.favorite_enabled);
        else
            favoriteButton.setBackgroundResource(R.drawable.favorite);
        if(retweeted)
            retweetButton.setBackgroundResource(R.drawable.retweet_enabled);
        else
            retweetButton.setBackgroundResource(R.drawable.retweet);
    }
}
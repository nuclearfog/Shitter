package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.util.Date;
import java.util.List;

import twitter4j.TwitterException;

import org.nuclearfog.twidda.database.TweetDatabase;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.window.ColorPreferences;
import org.nuclearfog.twidda.window.TweetDetail;
import org.nuclearfog.twidda.backend.listitems.*;
import org.nuclearfog.twidda.window.UserProfile;

public class StatusLoader extends AsyncTask<Long, Void, Long> {

    private static final long ERROR = -1;
    public static final long RETWEET = 0;
    public static final long FAVORITE = 1;
    public static final long DELETE = 2;
    public static final long LOAD_TWEET = 3;
    public static final long LOAD_REPLY = 4;

    private TwitterEngine mTwitter;
    private TimelineRecycler tlAdp;
    private RecyclerView replyList;
    private Bitmap profile_btm;
    private String usernameStr, scrNameStr, tweetStr, dateString;
    private String repliedUsername, apiName, retweeter;
    private String medialinks[];
    private String errMSG = "";
    private boolean retweeted, favorited, toggleImg, verified;
    private boolean rtFlag = false;
    private long tweetReplyID, userID;
    private int rt, fav;
    private int highlight, font;

    private WeakReference<TweetDetail> ui;

    public StatusLoader(Context c) {
        mTwitter = TwitterEngine.getInstance(c);
        SharedPreferences settings = c.getSharedPreferences("settings", 0);
        toggleImg = settings.getBoolean("image_load", true);
        ColorPreferences mColor = ColorPreferences.getInstance(c);
        highlight = mColor.getColor(ColorPreferences.HIGHLIGHTING);
        font = mColor.getColor(ColorPreferences.FONT_COLOR);

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
            Tweet tweet = mTwitter.getStatus(tweetID);
            Tweet embeddedTweet = tweet.embedded;
            if(embeddedTweet != null) {
                retweeter = "Retweet @"+tweet.screenname;
                tweet = mTwitter.getStatus(embeddedTweet.tweetID);
                tweetID = embeddedTweet.tweetID;
                rtFlag = true;
            }
            rt = tweet.retweet;
            fav = tweet.favorit;
            retweeted = tweet.retweeted;
            favorited = tweet.favorized;

            if(mode == LOAD_TWEET) {
                tweetReplyID = tweet.replyID;
                verified = tweet.verified;
                tweetStr = tweet.tweet;
                usernameStr = tweet.username;
                userID = tweet.userID;
                scrNameStr = '@'+tweet.screenname;
                apiName = formatString(tweet.source);
                dateString = DateFormat.getDateTimeInstance().format(new Date(tweet.time));
                repliedUsername = tweet.replyName;

                if(toggleImg) {
                    String pbLink = tweet.profileImg;
                    InputStream iStream = new URL(pbLink).openStream();
                    profile_btm = BitmapFactory.decodeStream(iStream);
                    medialinks = tweet.media;
                }
            }
            else if(mode == RETWEET) {
                if(retweeted) {
                    mTwitter.retweet(tweetID, true);
                    TweetDatabase.removeStatus(ui.get(), tweetID);
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
                List<Tweet> answers;
                String replyname = tweet.screenname;
                tlAdp = (TimelineRecycler) replyList.getAdapter();
                if(tlAdp != null && tlAdp.getItemCount() > 0) {
                    tweetID = tlAdp.getItemId(0);
                    answers = mTwitter.getAnswers(replyname, tweetID);
                    answers.addAll(tlAdp.getData());
                } else {
                    answers = mTwitter.getAnswers(replyname, tweetID);
                }
                tlAdp = new TimelineRecycler(answers,ui.get());
                tlAdp.setColor(highlight, font);
            }
            else if(mode == DELETE) {
                mTwitter.deleteTweet(tweetID);
                TweetDatabase.removeStatus(ui.get(),tweetID);
            }
        }catch(TwitterException e) {
            int err = e.getErrorCode();
            if(err == 144) { // gelöscht
                TweetDatabase.removeStatus(ui.get(),tweetID);
            }
            errMSG = e.getMessage();
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
        final Context c = connect;
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
                    intent.putExtra("username", repliedUsername);
                    c.startActivity(intent);
                }
            });
            profile_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ui.get(), UserProfile.class);
                    Bundle b = new Bundle();
                    b.putLong("userID",userID);
                    b.putString("username", usernameStr);
                    intent.putExtras(b);
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
            replyList.setAdapter(tlAdp);
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
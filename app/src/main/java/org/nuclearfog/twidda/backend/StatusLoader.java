package org.nuclearfog.twidda.backend;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import twitter4j.TwitterException;

import org.nuclearfog.twidda.database.DatabaseAdapter;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.viewadapter.TimelineRecycler;
import org.nuclearfog.twidda.window.ColorPreferences;
import org.nuclearfog.twidda.window.SearchPage;
import org.nuclearfog.twidda.window.TweetDetail;
import org.nuclearfog.twidda.backend.listitems.*;
import org.nuclearfog.twidda.window.UserProfile;

public class StatusLoader extends AsyncTask<Long, Void, Long> implements View.OnClickListener {

    private static final long ERROR = -1;
    public static final long RETWEET = 0;
    public static final long FAVORITE = 1;
    public static final long DELETE = 2;
    public static final long LOAD_TWEET = 3;
    public static final long LOAD_REPLY = 4;

    private TwitterEngine mTwitter;
    private TimelineRecycler tlAdp;
    private RecyclerView replyList;
    private String usernameStr, scrNameStr, tweetStr, dateString;
    private String repliedUsername, apiName, retweeter;
    private String medialinks[], profile_pb;
    private String errMSG = "";
    private boolean retweeted, favorited, toggleImg, verified;
    private boolean rtFlag = false;
    private long tweetReplyID, userID, retweeterID;
    private int rt, fav;
    private int highlight, font;

    private WeakReference<TweetDetail> ui;

    public StatusLoader(Context c) {
        mTwitter = TwitterEngine.getInstance(c);
        ColorPreferences mColor = ColorPreferences.getInstance(c);
        highlight = mColor.getColor(ColorPreferences.HIGHLIGHTING);
        font = mColor.getColor(ColorPreferences.FONT_COLOR);
        toggleImg = mColor.loadImage();
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
            if(tweet.embedded != null) {
                retweeter = "Retweet "+tweet.user.screenname;
                retweeterID = tweet.user.userID;
                tweet = tweet.embedded;
                tweetID = tweet.tweetID;
                rtFlag = true;
            }
            rt = tweet.retweet;
            fav = tweet.favorit;
            retweeted = tweet.retweeted;
            favorited = tweet.favorized;

            if(mode == LOAD_TWEET) {
                tweetReplyID = tweet.replyID;
                verified = tweet.user.isVerified;
                tweetStr = tweet.tweet;
                usernameStr = tweet.user.username;
                userID = tweet.user.userID;
                scrNameStr = tweet.user.screenname;
                apiName = formatString(tweet.source);
                dateString = DateFormat.getDateTimeInstance().format(new Date(tweet.time));
                repliedUsername = tweet.replyName;
                profile_pb = tweet.user.profileImg+"_bigger";
                medialinks = tweet.media;
            }
            else if(mode == RETWEET) {
                if(retweeted) {
                    mTwitter.retweet(tweetID, true);
                    new DatabaseAdapter(ui.get()).removeStatus(tweetID);
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
                String replyname = tweet.user.screenname;
                tlAdp = (TimelineRecycler) replyList.getAdapter();
                if(tlAdp != null && tlAdp.getItemCount() > 0) {
                    long sinceId = tlAdp.getItemId(0);
                    answers = mTwitter.getAnswers(replyname, tweetID, sinceId);
                    answers.addAll(tlAdp.getData());
                } else {
                    answers = mTwitter.getAnswers(replyname, tweetID, tweetID);
                }
                tlAdp = new TimelineRecycler(answers,ui.get());
                tlAdp.setColor(highlight, font);
            }
            else if(mode == DELETE) {
                mTwitter.deleteTweet(tweetID);
                new DatabaseAdapter(ui.get()).removeStatus(tweetID);
            }
        }catch(TwitterException e) {
            int err = e.getErrorCode();
            if(err == 144) { // gelöscht
                new DatabaseAdapter(ui.get()).removeStatus(tweetID);
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
            tweet.setMovementMethod(LinkMovementMethod.getInstance());
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
                replyName.setOnClickListener(this);
                replyName.setVisibility(View.VISIBLE);
            }
            if(rtFlag) {
                userRetweet.setText(retweeter);
                userRetweet.setOnClickListener(this);
                userRetweet.setVisibility(View.VISIBLE);
            }
            if(verified) {
                tweet_verify.setVisibility(View.VISIBLE);
            }
            if(toggleImg) {
                Picasso.with(ui.get()).load(profile_pb).into(profile_img);
                if(medialinks != null && medialinks.length != 0) {
                    mediabutton.setVisibility(View.VISIBLE);
                    mediabutton.setOnClickListener(this);
                }
            }
            setIcons(favoriteButton, retweetButton);
            profile_img.setOnClickListener(this);
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
            Toast.makeText(ui.get(), "Tweet gelöscht", Toast.LENGTH_LONG).show();
            ui.get().finish();
        }
        else {
            Toast.makeText(ui.get(), "Fehler beim Laden: "+errMSG, Toast.LENGTH_LONG).show();
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


    private Spannable highlight(String tweet) {
        Spannable sTweet = new SpannableStringBuilder(tweet);
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
                    if(marked && start != i-1) {
                        sTweet = spanning(sTweet, start, i);
                    }
                    marked = false;
                    break;
            }
        }
        if(marked && start != tweet.length()-1) {
            sTweet = spanning(sTweet, start, tweet.length());
        }
        return sTweet;
    }


    private Spannable spanning(Spannable sTweet, final int start, final int end) {
        sTweet.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                TextView tv = (TextView) widget;
                Spanned s = (Spanned) tv.getText();
                String search = s.subSequence(start, end).toString();
                Intent intent = new Intent(ui.get(), SearchPage.class);
                Bundle bundle = new Bundle();
                if(search.startsWith("#"))
                    bundle.putString("Addition", search);
                bundle.putString("search", search);
                intent.putExtras(bundle);
                ui.get().startActivity(intent);
            }
            @Override
            public void updateDrawState(TextPaint ds){
                ds.setColor(highlight);
                ds.setUnderlineText(false);
            }
        },start,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.profileimage_detail:
                Intent profile = new Intent(ui.get(), UserProfile.class);
                Bundle b = new Bundle();
                b.putLong("userID",userID);
                b.putString("username", scrNameStr);
                profile.putExtras(b);
                ui.get().startActivity(profile);
                break;

            case R.id.answer_reference_detail:
                Intent tweet = new Intent(ui.get(), TweetDetail.class);
                tweet.putExtra("tweetID",tweetReplyID);
                tweet.putExtra("username", '@'+repliedUsername);
                ui.get().startActivity(tweet);
                break;

            case R.id.image_attach:
                new ImagePopup(ui.get()).execute(medialinks);
                break;

            case R.id.rt_info:
                Intent rProfile = new Intent(ui.get(), UserProfile.class);
                Bundle extras = new Bundle();
                extras.putLong("userID",retweeterID);
                extras.putString("username", scrNameStr);
                rProfile.putExtras(extras);
                ui.get().startActivity(rProfile);
                break;
        }
    }
}
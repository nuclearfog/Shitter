package org.nuclearfog.twidda.viewadapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.window.ColorPreferences;
import org.nuclearfog.twidda.database.TweetDatabase;

public class TimelineAdapter extends ArrayAdapter implements View.OnClickListener {
    private TweetDatabase mTweets;
    private ViewGroup p;
    private LayoutInflater inf;
    private int textColor, background, highlight;
    private Context context;

    public TimelineAdapter(Context context, TweetDatabase mTweets) {
        super(context, R.layout.tweet);
        this.mTweets = mTweets;
        this.context = context;
        inf = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ColorPreferences mColor = ColorPreferences.getInstance(context);
        textColor = mColor.getColor(ColorPreferences.FONT_COLOR);
        background = mColor.getColor(ColorPreferences.BACKGROUND);
        highlight = mColor.getColor(ColorPreferences.HIGHLIGHTING);
    }

    /**
     * Fetch & Update Data
     * @return TweetDatabase instance
     */
    public TweetDatabase getData() {
        return mTweets;
    }

    @Override
    public int getCount() {
        return mTweets.getSize();
    }

    @Override
    public long getItemId(int pos){
        return mTweets.getTweetId(pos);
    }

    @NonNull
    @Override
    public View getView(int position, View v, @NonNull ViewGroup parent) {
        p = parent;
        if(v == null) {
            v = inf.inflate(R.layout.tweet, parent,false);
            v.setBackgroundColor(background);
            v.setOnClickListener(this);
        }
        String answerStr = Integer.toString(mTweets.getAnswer(position));
        String retweetStr = Integer.toString(mTweets.getRetweet(position));
        String favoriteStr = Integer.toString(mTweets.getFavorite(position));

        ((TextView) v.findViewById(R.id.username)).setText(mTweets.getUsername(position));
        ((TextView) v.findViewById(R.id.screenname)).setText(mTweets.getScreenname(position));
        ((TextView) v.findViewById(R.id.tweettext)).setText(highlight(mTweets.getTweet(position)));
        ((TextView) v.findViewById(R.id.answer_number)).setText(answerStr);
        ((TextView) v.findViewById(R.id.retweet_number)).setText(retweetStr);
        ((TextView) v.findViewById(R.id.favorite_number)).setText(favoriteStr);
        ((TextView) v.findViewById(R.id.time)).setText(mTweets.getDate(position));
        ((TextView) v.findViewById(R.id.tweettext)).setTextColor(textColor);
        ImageView pb = v.findViewById(R.id.tweetPb);
        if(mTweets.loadImages()) {
            Picasso.with(context).load(mTweets.getPbLink(position)).into(pb);
        }
        return v;
    }

    @Override
    public void onClick(View v) {
        ListView parent = ((ListView)p);
        int position = parent.getPositionForView(v);
        parent.performItemClick(v,position,0);
    }

    private SpannableStringBuilder highlight(String tweet) {
        SpannableStringBuilder sTweet = new SpannableStringBuilder(tweet);
        int start = 0;
        boolean marked = false;
        for(int i = 0 ; i < tweet.length() ; i++) {
            char current = tweet.charAt(i);
            switch(current){
                case '@':
                    start = i;
                    marked = true;
                    break;
                case '#':
                    start = i;
                    marked = true;
                    break;

                case '\'':
                case ':':
                case ' ':
                case '.':
                case ',':
                case '!':
                case '?':
                    if(marked)
                        sTweet.setSpan(new ForegroundColorSpan(highlight),start,i, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    marked = false;
                    break;
            }
            if(i == tweet.length()-1 && marked) {
                sTweet.setSpan(new ForegroundColorSpan(highlight),start,i+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return sTweet;
    }
}
package org.nuclearfog.twidda.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.graphics.PorterDuff.Mode.SRC_IN;

/**
 * Holder class for the tweet view
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.TweetAdapter
 */
public class TweetHolder extends ViewHolder {

    public final TextView[] textViews = new TextView[7];
    public final ImageView profile, rtUser, verifiedIcon, lockedIcon, rtIcon, favIcon;

    /**
     * @param parent Parent view from adapter
     */
    public TweetHolder(ViewGroup parent, GlobalSettings settings) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tweet, parent, false));
        // get views
        CardView background = (CardView) itemView;
        profile = itemView.findViewById(R.id.tweetPb);
        verifiedIcon = itemView.findViewById(R.id.verified_icon);
        lockedIcon = itemView.findViewById(R.id.locked_icon);
        rtUser = itemView.findViewById(R.id.rt_user_icon);
        rtIcon = itemView.findViewById(R.id.rt_icon);
        favIcon = itemView.findViewById(R.id.fav_icon);
        textViews[0] = itemView.findViewById(R.id.username);
        textViews[1] = itemView.findViewById(R.id.screenname);
        textViews[2] = itemView.findViewById(R.id.tweettext);
        textViews[3] = itemView.findViewById(R.id.retweet_number);
        textViews[4] = itemView.findViewById(R.id.favorite_number);
        textViews[5] = itemView.findViewById(R.id.retweeter);
        textViews[6] = itemView.findViewById(R.id.time);
        // set icons
        verifiedIcon.setImageResource(R.drawable.verify);
        lockedIcon.setImageResource(R.drawable.lock);
        rtUser.setImageResource(R.drawable.retweet);
        rtIcon.setImageResource(R.drawable.retweet);
        favIcon.setImageResource(R.drawable.favorite);
        // theme views
        verifiedIcon.setColorFilter(settings.getIconColor(), SRC_IN);
        lockedIcon.setColorFilter(settings.getIconColor(), SRC_IN);
        rtUser.setColorFilter(settings.getIconColor(), SRC_IN);
        background.setCardBackgroundColor(settings.getCardColor());
        for (TextView tv : textViews) {
            tv.setTextColor(settings.getFontColor());
            tv.setTypeface(settings.getTypeFace());
        }
    }
}
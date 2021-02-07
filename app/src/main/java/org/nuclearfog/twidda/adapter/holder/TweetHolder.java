package org.nuclearfog.twidda.adapter.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.graphics.PorterDuff.Mode.SRC_IN;

/**
 * Holder class for the tweet view
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.TweetAdapter
 */
public class TweetHolder extends RecyclerView.ViewHolder {
    public final TextView[] textViews = new TextView[7];
    public final ImageView profile, rtUser, verifiedIcon, lockedIcon, rtIcon, favIcon;

    public TweetHolder(@NonNull View v, GlobalSettings settings) {
        super(v);
        CardView background = (CardView) v;
        profile = v.findViewById(R.id.tweetPb);
        verifiedIcon = v.findViewById(R.id.verified_icon);
        lockedIcon = v.findViewById(R.id.locked_icon);
        rtUser = v.findViewById(R.id.rt_user_icon);
        rtIcon = v.findViewById(R.id.rt_icon);
        favIcon = v.findViewById(R.id.fav_icon);
        textViews[0] = v.findViewById(R.id.username);
        textViews[1] = v.findViewById(R.id.screenname);
        textViews[2] = v.findViewById(R.id.tweettext);
        textViews[3] = v.findViewById(R.id.retweet_number);
        textViews[4] = v.findViewById(R.id.favorite_number);
        textViews[5] = v.findViewById(R.id.retweeter);
        textViews[6] = v.findViewById(R.id.time);

        verifiedIcon.setImageResource(R.drawable.verify);
        lockedIcon.setImageResource(R.drawable.lock);
        rtUser.setImageResource(R.drawable.retweet);
        rtIcon.setImageResource(R.drawable.retweet);
        favIcon.setImageResource(R.drawable.favorite);
        verifiedIcon.setColorFilter(settings.getIconColor(), SRC_IN);
        lockedIcon.setColorFilter(settings.getIconColor(), SRC_IN);
        rtUser.setColorFilter(settings.getIconColor(), SRC_IN);
        background.setCardBackgroundColor(settings.getCardColor());

        for (TextView tv : textViews) {
            tv.setTextColor(settings.getFontColor());
            tv.setTypeface(settings.getFontFace());
        }
    }
}
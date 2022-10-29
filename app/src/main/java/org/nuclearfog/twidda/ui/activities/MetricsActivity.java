package org.nuclearfog.twidda.ui.activities;

import static android.widget.Toast.LENGTH_SHORT;
import static org.nuclearfog.twidda.ui.activities.SearchActivity.KEY_SEARCH_QUERY;
import static org.nuclearfog.twidda.ui.activities.TweetActivity.KEY_TWEET_ID;
import static org.nuclearfog.twidda.ui.activities.TweetActivity.KEY_TWEET_NAME;
import static org.nuclearfog.twidda.ui.activities.TweetActivity.LINK_PATTERN;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import com.squareup.picasso.Picasso;

import org.nuclearfog.tag.Tagger;
import org.nuclearfog.tag.Tagger.OnTagClickListener;
import org.nuclearfog.textviewtool.LinkAndScrollMovement;
import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.api.twitter.TwitterException;
import org.nuclearfog.twidda.backend.async.MetricsLoader;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.ErrorHandler;
import org.nuclearfog.twidda.backend.utils.PicassoBuilder;
import org.nuclearfog.twidda.backend.utils.RefreshDelay;
import org.nuclearfog.twidda.backend.utils.RefreshDelay.RefreshCallback;
import org.nuclearfog.twidda.database.GlobalSettings;
import org.nuclearfog.twidda.model.Metrics;
import org.nuclearfog.twidda.model.Tweet;
import org.nuclearfog.twidda.model.User;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * This activity shows metrics of a tweet (views, link clicks, etc.)
 *
 * @author nuclearfog
 */
public class MetricsActivity extends AppCompatActivity implements OnClickListener, OnTagClickListener, OnRefreshListener, RefreshCallback {

	/**
	 * key used for tweet information
	 * value type is {@link Tweet}
	 */
	public static final String KEY_METRICS_TWEET = "metrics_tweet";

	/**
	 * delay to enable SwipeRefreshLayout
	 */
	private static final int REFRESH_DELAY_MS = 1000;

	private static final NumberFormat NUM_FORMAT = NumberFormat.getIntegerInstance();

	private MetricsLoader metricsAsync;

	private SwipeRefreshLayout reload;
	private TextView impressionCount;
	private TextView linkClicks;
	private TextView profileClicks;
	private TextView retweetCount;
	private TextView favoriteCount;
	private TextView replycount;
	private TextView quoteCount;
	private TextView videoViews;
	@Nullable
	private Tweet tweet;

	private boolean isRefreshing = false;

	@Override
	protected void onCreate(Bundle savedInst) {
		super.onCreate(savedInst);
		setContentView(R.layout.page_metrics);

		ViewGroup root = findViewById(R.id.metrics_root);
		Toolbar toolbar = findViewById(R.id.metrics_toolbar);
		ImageView profile = findViewById(R.id.metrics_profile);
		TextView username = findViewById(R.id.metrics_username);
		TextView screenname = findViewById(R.id.metrics_screenname);
		TextView tweetText = findViewById(R.id.metrics_tweet);
		TextView created = findViewById(R.id.metrics_created);
		impressionCount = findViewById(R.id.metrics_impression);
		linkClicks = findViewById(R.id.metrics_link_count);
		profileClicks = findViewById(R.id.metrics_profile_count);
		retweetCount = findViewById(R.id.metrics_retweets);
		favoriteCount = findViewById(R.id.metrics_favorits);
		replycount = findViewById(R.id.metrics_replies);
		quoteCount = findViewById(R.id.metrics_quotes);
		videoViews = findViewById(R.id.metrics_video_clicks);
		reload = findViewById(R.id.metrics_refresh);

		Picasso picasso = PicassoBuilder.get(this);
		GlobalSettings settings = GlobalSettings.getInstance(this);

		impressionCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.views, 0, 0, 0);
		linkClicks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.link, 0, 0, 0);
		profileClicks.setCompoundDrawablesWithIntrinsicBounds(R.drawable.user, 0, 0, 0);
		retweetCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.retweet, 0, 0, 0);
		replycount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.answer, 0, 0, 0);
		quoteCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.quote, 0, 0, 0);
		videoViews.setCompoundDrawablesWithIntrinsicBounds(R.drawable.play, 0, 0, 0);
		if (settings.likeEnabled()) {
			favoriteCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.like, 0, 0, 0);
		} else {
			favoriteCount.setCompoundDrawablesWithIntrinsicBounds(R.drawable.favorite, 0, 0, 0);
		}
		AppStyles.setTheme(root, settings.getBackgroundColor());
		AppStyles.setSwipeRefreshColor(reload, settings);
		tweetText.setMovementMethod(LinkAndScrollMovement.getInstance());
		toolbar.setTitle(R.string.title_metrics);

		Serializable data = getIntent().getSerializableExtra(KEY_METRICS_TWEET);
		if (data instanceof Tweet) {
			tweet = (Tweet) data;
			User author = tweet.getAuthor();
			if (settings.imagesEnabled() && !author.getImageUrl().isEmpty()) {
				String profileImageUrl = author.getImageUrl();
				if (!author.hasDefaultProfileImage())
					profileImageUrl += settings.getImageSuffix();
				picasso.load(profileImageUrl).transform(new RoundedCornersTransformation(4, 0))
						.error(R.drawable.no_image).into(profile);
			} else {
				profile.setImageResource(0);
			}
			if (author.isVerified()) {
				username.setCompoundDrawablesWithIntrinsicBounds(R.drawable.verify, 0, 0, 0);
				AppStyles.setDrawableColor(username, settings.getIconColor());
			}
			if (author.isProtected()) {
				screenname.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
				AppStyles.setDrawableColor(screenname, settings.getIconColor());
			}
			username.setText(author.getUsername());
			screenname.setText(author.getScreenname());
			tweetText.setText(Tagger.makeTextWithLinks(tweet.getText(), settings.getHighlightColor(), this));
			created.setText(SimpleDateFormat.getDateTimeInstance().format(tweet.getTimestamp()));
		}
		profile.setOnClickListener(this);
		reload.setOnRefreshListener(this);
	}


	@Override
	protected void onStart() {
		super.onStart();
		if (metricsAsync == null && tweet != null) {
			metricsAsync = new MetricsLoader(this);
			metricsAsync.execute(tweet.getId());
			setRefresh(true);
		}
	}


	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.metrics_profile) {
			if (tweet != null) {
				Intent profile = new Intent(getApplicationContext(), ProfileActivity.class);
				profile.putExtra(ProfileActivity.KEY_PROFILE_DATA, tweet.getAuthor());
				startActivity(profile);
			}
		}
	}


	@Override
	public void onRefresh() {
		if (tweet != null) {
			metricsAsync = new MetricsLoader(this);
			metricsAsync.execute(tweet.getId());
		}
	}


	@Override
	public void onTagClick(String tag) {
		Intent intent = new Intent(this, SearchActivity.class);
		intent.putExtra(KEY_SEARCH_QUERY, tag);
		startActivity(intent);
	}


	@Override
	public void onLinkClick(String tag) {
		Uri link = Uri.parse(tag);
		// open tweet link
		if (LINK_PATTERN.matcher(link.getScheme() + "://" + link.getHost() + link.getPath()).matches()) {
			List<String> segments = link.getPathSegments();
			Intent intent = new Intent(this, TweetActivity.class);
			intent.putExtra(KEY_TWEET_ID, Long.parseLong(segments.get(2)));
			intent.putExtra(KEY_TWEET_NAME, segments.get(0));
			startActivity(intent);
		}
		// open link in browser
		else {
			Intent intent = new Intent(Intent.ACTION_VIEW, link);
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException err) {
				Toast.makeText(this, R.string.error_connection_failed, LENGTH_SHORT).show();
			}
		}
	}

	/**
	 *
	 */
	@Override
	public void onRefreshDelayed() {
		if (isRefreshing && !reload.isRefreshing()) {
			reload.setRefreshing(true);
		}
	}

	/**
	 * called from {@link MetricsLoader} if metrics was loaded sucessfully
	 *
	 * @param metrics of a specific tweet
	 */
	public void onSuccess(Metrics metrics) {
		impressionCount.setText(NUM_FORMAT.format(metrics.getViews()));
		impressionCount.setVisibility(View.VISIBLE);
		if (metrics.getLinkClicks() > 0) {
			linkClicks.setText(NUM_FORMAT.format(metrics.getLinkClicks()));
			linkClicks.setVisibility(View.VISIBLE);
		}
		if (metrics.getProfileClicks() > 0) {
			profileClicks.setText(NUM_FORMAT.format(metrics.getProfileClicks()));
			profileClicks.setVisibility(View.VISIBLE);
		}
		if (metrics.getRetweets() > 0) {
			retweetCount.setText(NUM_FORMAT.format(metrics.getRetweets()));
			retweetCount.setVisibility(View.VISIBLE);
		}
		if (metrics.getLikes() > 0) {
			favoriteCount.setText(NUM_FORMAT.format(metrics.getLikes()));
			favoriteCount.setVisibility(View.VISIBLE);
		}
		if (metrics.getReplies() > 0) {
			replycount.setText(NUM_FORMAT.format(metrics.getReplies()));
			replycount.setVisibility(View.VISIBLE);
		}
		if (metrics.getQuoteCount() > 0) {
			quoteCount.setText(NUM_FORMAT.format(metrics.getQuoteCount()));
			quoteCount.setVisibility(View.VISIBLE);
		}
		if (metrics.getVideoViews() > 0) {
			videoViews.setText(NUM_FORMAT.format(metrics.getVideoViews()));
			videoViews.setVisibility(View.VISIBLE);
		}
		setRefresh(false);
	}

	/**
	 * called from {@link MetricsLoader} if an error occurs
	 */
	public void onError(@Nullable TwitterException exception) {
		ErrorHandler.handleFailure(this, exception);
		setRefresh(false);
	}

	/**
	 *
	 */
	private void setRefresh(boolean enable) {
		isRefreshing = enable;
		if (enable) {
			reload.postDelayed(new RefreshDelay(this), REFRESH_DELAY_MS);
		} else {
			reload.setRefreshing(false);
		}
	}
}
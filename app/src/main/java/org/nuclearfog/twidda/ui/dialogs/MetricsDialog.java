package org.nuclearfog.twidda.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringTools;
import org.nuclearfog.twidda.model.Metrics;

/**
 * Status metrics dialog
 *
 * @author nuclearfog
 */
public class MetricsDialog extends Dialog {

	private TextView views, profileClicks, linkClicks, quotes, videoViews;
	private View linkIcon, quoteIcon, videoIcon;


	public MetricsDialog(@NonNull Context context) {
		super(context, R.style.MetricsDialog);
		setContentView(R.layout.dialog_metrics);
		ViewGroup root = findViewById(R.id.dialog_metrics_root);
		views = findViewById(R.id.dialog_metrics_views);
		profileClicks = findViewById(R.id.dialog_metrics_profile_clicks);
		linkClicks = findViewById(R.id.dialog_metrics_link_clicks);
		quotes = findViewById(R.id.dialog_metrics_quotes);
		videoViews = findViewById(R.id.dialog_metrics_video_view);
		linkIcon = findViewById(R.id.dialog_metrics_link_clicks_icon);
		quoteIcon = findViewById(R.id.dialog_metrics_quotes_icon);
		videoIcon = findViewById(R.id.dialog_metrics_video_view_icon);
		AppStyles.setTheme(root);
	}


	@Override
	public void show() {
	}

	/**
	 * show dialog window
	 *
	 * @param metrics status metrics to show
	 */
	public void show(Metrics metrics) {
		if (!isShowing()) {
			views.setText(StringTools.NUMBER_FORMAT.format(metrics.getViews()));
			profileClicks.setText(StringTools.NUMBER_FORMAT.format(metrics.getProfileClicks()));
			if (metrics.getLinkClicks() > 0) {
				linkClicks.setText(StringTools.NUMBER_FORMAT.format(metrics.getLinkClicks()));
				linkClicks.setVisibility(View.VISIBLE);
				linkIcon.setVisibility(View.VISIBLE);
			} else {
				linkClicks.setVisibility(View.GONE);
				linkIcon.setVisibility(View.GONE);
			}
			if (metrics.getQuoteCount() > 0) {
				quotes.setText(StringTools.NUMBER_FORMAT.format(metrics.getQuoteCount()));
				quotes.setVisibility(View.VISIBLE);
				quoteIcon.setVisibility(View.VISIBLE);
			} else {
				quotes.setVisibility(View.GONE);
				quoteIcon.setVisibility(View.GONE);
			}
			if (metrics.getVideoViews() > 0) {
				videoViews.setText(StringTools.NUMBER_FORMAT.format(metrics.getVideoViews()));
				videoViews.setVisibility(View.VISIBLE);
				videoIcon.setVisibility(View.VISIBLE);
			} else {
				videoViews.setVisibility(View.GONE);
				videoIcon.setVisibility(View.GONE);
			}
			super.show();
		}
	}
}
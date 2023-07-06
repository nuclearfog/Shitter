package org.nuclearfog.twidda.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.backend.utils.StringUtils;
import org.nuclearfog.twidda.model.Metrics;

import java.io.Serializable;

/**
 * Status metrics dialog
 *
 * @author nuclearfog
 */
public class MetricsDialog extends Dialog {

	private static final String KEY_SAVE = "metrics-save";

	private TextView views, profileClicks, linkClicks, quotes, videoViews;
	private View linkIcon, quoteIcon, videoIcon;

	private Metrics metrics;

	/**
	 *
	 */
	public MetricsDialog(Activity activity) {
		super(activity, R.style.DefaultDialog);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
	protected void onStart() {
		super.onStart();
		if (metrics != null) {
			views.setText(StringUtils.NUMBER_FORMAT.format(metrics.getViews()));
			profileClicks.setText(StringUtils.NUMBER_FORMAT.format(metrics.getProfileClicks()));
			if (metrics.getLinkClicks() > 0) {
				linkClicks.setText(StringUtils.NUMBER_FORMAT.format(metrics.getLinkClicks()));
				linkClicks.setVisibility(View.VISIBLE);
				linkIcon.setVisibility(View.VISIBLE);
			} else {
				linkClicks.setVisibility(View.GONE);
				linkIcon.setVisibility(View.GONE);
			}
			if (metrics.getQuoteCount() > 0) {
				quotes.setText(StringUtils.NUMBER_FORMAT.format(metrics.getQuoteCount()));
				quotes.setVisibility(View.VISIBLE);
				quoteIcon.setVisibility(View.VISIBLE);
			} else {
				quotes.setVisibility(View.GONE);
				quoteIcon.setVisibility(View.GONE);
			}
			if (metrics.getVideoViews() > 0) {
				videoViews.setText(StringUtils.NUMBER_FORMAT.format(metrics.getVideoViews()));
				videoViews.setVisibility(View.VISIBLE);
				videoIcon.setVisibility(View.VISIBLE);
			} else {
				videoViews.setVisibility(View.GONE);
				videoIcon.setVisibility(View.GONE);
			}
		}
	}


	@NonNull
	@Override
	public Bundle onSaveInstanceState() {
		Bundle bundle = super.onSaveInstanceState();
		bundle.putSerializable(KEY_SAVE, metrics);
		return bundle;
	}


	@Override
	public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		Serializable data = savedInstanceState.getSerializable(KEY_SAVE);
		if (data instanceof Metrics) {
			metrics = (Metrics) data;
		}
		super.onRestoreInstanceState(savedInstanceState);
	}


	@Override
	public void show() {
		// using show(Metrics) instead
	}


	@Override
	public void dismiss() {
		if (isShowing()) {
			super.dismiss();
		}
	}

	/**
	 * show dialog window
	 *
	 * @param metrics status metrics to show
	 */
	public void show(Metrics metrics) {
		if (!isShowing()) {
			this.metrics = metrics;
			super.show();
		}
	}
}
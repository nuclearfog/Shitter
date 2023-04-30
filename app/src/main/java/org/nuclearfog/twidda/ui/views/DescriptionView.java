package org.nuclearfog.twidda.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.config.GlobalSettings;

/**
 * Description label view used to show image description
 *
 * @author nuclearfog
 */
public class DescriptionView extends LinearLayout implements OnClickListener {

	/**
	 * background transparency color mask
	 */
	private static final int TRANSPARENCY_MASK = 0x7FFFFFFF;

	private ImageView closeButton;
	private TextView label;

	/**
	 * @inheritDoc
	 */
	public DescriptionView(Context context) {
		this(context, null);
	}

	/**
	 * @inheritDoc
	 */
	public DescriptionView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		GlobalSettings settings = GlobalSettings.getInstance(context);
		int padding = (int) getResources().getDimension(R.dimen.descriptionview_layout_padding);
		int displayWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
		closeButton = new ImageView(context);
		label = new TextView(context);

		Drawable background = ResourcesCompat.getDrawable(getResources(), R.drawable.background, null);
		Drawable closeIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.cross, null);
		if (background != null)
			background.setColorFilter(settings.getBackgroundColor() & TRANSPARENCY_MASK, PorterDuff.Mode.SRC_IN);
		if (closeIcon != null)
			closeIcon.setColorFilter(settings.getIconColor(), PorterDuff.Mode.SRC_IN);

		label.setPadding(padding, 0, 0, 0);
		label.setMaxLines(4);
		label.setMaxWidth(displayWidth / 2);
		label.setTextColor(settings.getTextColor());
		closeButton.setImageDrawable(closeIcon);

		setBackground(background);
		setPadding(padding, padding, padding, padding);
		setOrientation(HORIZONTAL);
		addView(closeButton);
		addView(label);

		closeButton.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		if (v == closeButton) {
			setVisibility(INVISIBLE);
		}
	}

	/**
	 * set description
	 *
	 * @param description description text
	 */
	public void setDescription(String description) {
		label.setText(description);
	}
}
package org.nuclearfog.twidda.ui.views;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.net.Uri;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * custom {@link android.widget.ImageView} implementation to support animated images
 *
 * @author nuclearfog
 */
public class AnimatedImageView extends AppCompatImageView {

	/**
	 * image upscale limitation
	 */
	private static final float MAX_SCALE = 10.0f;

	private Movie movie;
	private float xOffset, yOffset;
	private float scale = 0.0f;
	private long moviestart = 0;

	/**
	 * @inheritDoc
	 */
	public AnimatedImageView(Context context) {
		this(context, null);
	}

	/**
	 * @inheritDoc
	 */
	public AnimatedImageView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void setImageURI(@Nullable Uri uri) {
		ContentResolver resolver = getContext().getContentResolver();
		String mime = resolver.getType(uri);
		if (mime != null && mime.equals("image/gif")) {
			try {
				InputStream is = resolver.openInputStream(uri);
				movie = Movie.decodeStream(is);
			} catch (FileNotFoundException e) {
				// ignore, use static image
			}
		}
		super.setImageURI(uri);
	}

	/**
	 * @inheritDoc
	 */
	@Override
	@SuppressWarnings("deprecation")
	protected void onDraw(Canvas canvas) {
		if (movie != null) {
			// calculate scale and offsets
			if (scale == 0.0f && movie.height() > 0 && movie.width() > 0) {
				scale = Math.min((float) getMeasuredHeight() / (float) movie.height(), (float) getMeasuredWidth() / (float) movie.width());
				scale = Math.min(scale, MAX_SCALE);
				if (scale > 0.0) {
					xOffset = ((float) getWidth() / scale - (float) movie.width()) / 2.0f;
					yOffset = ((float) getHeight() / scale - (float) movie.height()) / 2.0f;
				}
			}
			long now = System.currentTimeMillis();
			if (moviestart == 0)
				moviestart = now;
			// set relative time
			movie.setTime((int) ((now - moviestart) % movie.duration()));
			// scale, translate and draw canvas
			if (scale != 0.0f)
				canvas.scale(scale, scale);
			movie.draw(canvas, xOffset, yOffset);
			// trigger next drawing
			invalidate();
		} else {
			super.onDraw(canvas);
		}
	}
}
package org.nuclearfog.twidda.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RemoteViews.RemoteView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import org.nuclearfog.twidda.R;

/**
 * Zoomable image view
 *
 * @author nuclearfog
 */
@RemoteView
public class ZoomView extends AppCompatImageView {

	// Default values
	private static final float DEF_MAX_ZOOM_IN = 3.0f;
	private static final float DEF_MAX_ZOOM_OUT = 0.5f;
	private static final boolean DEF_ENABLE_MOVE = true;
	private static final ScaleType DEF_SCALE_TYPE = ScaleType.FIT_CENTER;

	// Layout Attributes
	private float max_zoom_in = DEF_MAX_ZOOM_IN;
	private float max_zoom_out = DEF_MAX_ZOOM_OUT;
	private boolean enableMove = DEF_ENABLE_MOVE;
	private ScaleType scaleType = DEF_SCALE_TYPE;

	// intern flags
	private final PointF pos = new PointF(0.0f, 0.0f);
	private final PointF dist = new PointF(0.0f, 0.0f);
	private boolean moveLock = false;

	/**
	 *
	 */
	public ZoomView(Context context) {
		super(context);
	}

	/**
	 *
	 */
	public ZoomView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 *
	 */
	public ZoomView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		scaleType = getScaleType();
		if (attrs != null) {
			TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ZoomView);
			setMaxZoomIn(attrArray.getFloat(R.styleable.ZoomView_max_zoom_in, DEF_MAX_ZOOM_IN));
			setMaxZoomOut(attrArray.getFloat(R.styleable.ZoomView_max_zoom_out, DEF_MAX_ZOOM_OUT));
			setMovable(attrArray.getBoolean(R.styleable.ZoomView_enable_move, DEF_ENABLE_MOVE));
			attrArray.recycle();
		}
	}


	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (getScaleType() != ScaleType.MATRIX)
			setScaleType(ScaleType.MATRIX);
		if (event.getPointerCount() == 1) {
			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_UP:
					pos.set(event.getX(), event.getY());
					moveLock = false;
					break;

				case MotionEvent.ACTION_DOWN:
					pos.set(event.getX(), event.getY());
					break;

				case MotionEvent.ACTION_MOVE:
					if (moveLock || !enableMove)
						return super.performClick();
					float posX = event.getX() - pos.x;
					float posY = event.getY() - pos.y;
					pos.set(event.getX(), event.getY());
					Matrix m = new Matrix(getImageMatrix());
					m.postTranslate(posX, posY);
					apply(m);
					break;
			}
		} else if (event.getPointerCount() == 2) {
			float distX, distY, scale;
			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_POINTER_UP:
				case MotionEvent.ACTION_POINTER_DOWN:
					distX = event.getX(0) - event.getX(1);
					distY = event.getY(0) - event.getY(1);
					dist.set(distX, distY);                     // Distance vector
					moveLock = true;
					break;

				case MotionEvent.ACTION_MOVE:
					distX = event.getX(0) - event.getX(1);
					distY = event.getY(0) - event.getY(1);
					PointF current = new PointF(distX, distY);
					scale = current.length() / dist.length();
					Matrix m = new Matrix(getImageMatrix());
					m.postScale(scale, scale, getWidth() / 2.0f, getHeight() / 2.0f);
					dist.set(distX, distY);
					apply(m);
					break;
			}
		}
		return true;
	}

	/**
	 * Reset Image position/zoom to default
	 */
	public void reset() {
		setScaleType(scaleType);
	}

	/**
	 * set Image movable
	 *
	 * @param enableMove set image movable
	 */
	public void setMovable(boolean enableMove) {
		this.enableMove = enableMove;
	}

	/**
	 * set maximum zoom in
	 *
	 * @param max_zoom_in maximum zoom value
	 */
	public void setMaxZoomIn(float max_zoom_in) {
		if (max_zoom_in < 1.0f)
			throw new AssertionError("value should be more 1.0!");
		this.max_zoom_in = max_zoom_in;
	}

	/**
	 * set maximum zoom in
	 *
	 * @param max_zoom_out maximum zoom value
	 */
	public void setMaxZoomOut(float max_zoom_out) {
		if (max_zoom_out > 1.0f)
			throw new AssertionError("value should be less 1.0!");
		this.max_zoom_out = max_zoom_out;
	}

	/**
	 *
	 */
	private void apply(Matrix m) {
		Drawable d = getDrawable();
		if (d == null)
			return;

		float[] val = new float[9];
		m.getValues(val);
		float scale = (val[Matrix.MSCALE_X] + val[Matrix.MSCALE_Y]) / 2;    // Scale factor
		float width = d.getIntrinsicWidth() * scale;                        // image width
		float height = d.getIntrinsicHeight() * scale;                      // image height
		float leftBorder = val[Matrix.MTRANS_X];                            // distance to left border
		float rightBorder = -(val[Matrix.MTRANS_X] + width - getWidth());   // distance to right border
		float bottomBorder = val[Matrix.MTRANS_Y];                          // distance to bottom border
		float topBorder = -(val[Matrix.MTRANS_Y] + height - getHeight());   // distance to top border

		if (width > getWidth()) {                       // is image width bigger than screen width?
			if (rightBorder > 0)                        // is image on the right border?
				m.postTranslate(rightBorder, 0);        // clamp to right border
			else if (leftBorder > 0)
				m.postTranslate(-leftBorder, 0);        // clamp to left order
		} else if (leftBorder < 0 ^ rightBorder < 0) {  // does image clash with one border?
			if (rightBorder < 0)
				m.postTranslate(rightBorder, 0);        // clamp to right border
			else
				m.postTranslate(-leftBorder, 0);        // clamp to left border
		}
		if (height > getHeight()) {                     // is image height bigger than screen height?
			if (bottomBorder > 0)                       // is image on the bottom border?
				m.postTranslate(0, -bottomBorder);      // clamp to bottom border
			else if (topBorder > 0)                     // is image on the top border?
				m.postTranslate(0, topBorder);          // clamp to top border
		} else if (topBorder < 0 ^ bottomBorder < 0) {  // does image clash with one border?
			if (bottomBorder < 0)
				m.postTranslate(0, -bottomBorder);      // clamp to bottom border
			else
				m.postTranslate(0, topBorder);          // clamp to top border
		}
		if (scale > max_zoom_in) {                      // scale limit exceeded?
			float undoScale = max_zoom_in / scale;      // undo scale setting
			m.postScale(undoScale, undoScale, getWidth() / 2.0f, getHeight() / 2.0f);
		} else if (scale < max_zoom_out) {              // scale limit exceeded?
			float undoScale = max_zoom_out / scale;     // undo scale setting
			m.postScale(undoScale, undoScale, getWidth() / 2.0f, getHeight() / 2.0f);
		}
		setImageMatrix(m);                              // set Image matrix
	}
}
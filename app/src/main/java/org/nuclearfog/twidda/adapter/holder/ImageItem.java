package org.nuclearfog.twidda.adapter.holder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * Holder for image item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.ImageAdapter
 */
public final class ImageItem extends RecyclerView.ViewHolder {

    public final ImageView preview;
    public final ImageButton saveButton;

    public ImageItem(View view, GlobalSettings settings) {
        super(view);
        CardView cardBackground = (CardView) view;
        preview = view.findViewById(R.id.item_image_preview);
        saveButton = view.findViewById(R.id.item_image_save);
        saveButton.setImageResource(R.drawable.save);
        cardBackground.setCardBackgroundColor(settings.getCardColor());
        AppStyles.setButtonColor(saveButton, settings.getFontColor());
        AppStyles.setDrawableColor(saveButton, settings.getIconColor());
    }
}
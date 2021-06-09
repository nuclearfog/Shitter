package org.nuclearfog.twidda.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

/**
 * Holder for image item
 *
 * @author nuclearfog
 * @see org.nuclearfog.twidda.adapter.ImageAdapter
 */
public class ImageItem extends ViewHolder {

    public final ImageView preview;
    public final ImageButton saveButton;

    /**
     * @param parent Parent view from adapter
     */
    public ImageItem(ViewGroup parent, GlobalSettings settings) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false));
        // get views
        CardView cardBackground = (CardView) itemView;
        preview = itemView.findViewById(R.id.item_image_preview);
        saveButton = itemView.findViewById(R.id.item_image_save);
        // set icon
        saveButton.setImageResource(R.drawable.save);
        // theme views
        cardBackground.setCardBackgroundColor(settings.getCardColor());
        AppStyles.setButtonColor(saveButton, settings.getFontColor());
        AppStyles.setDrawableColor(saveButton, settings.getIconColor());
    }
}
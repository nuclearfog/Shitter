package org.nuclearfog.twidda.adapter.holder;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.nuclearfog.twidda.R;
import org.nuclearfog.twidda.backend.utils.AppStyles;
import org.nuclearfog.twidda.database.GlobalSettings;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * ViewHolder class for a footer/placeholder view
 *
 * @author nuclearfog
 */
public class Footer extends RecyclerView.ViewHolder {

    public final ProgressBar loadCircle;
    public final Button loadBtn;

    /**
     * @param v          inflated view R.layout.item_placeholder
     * @param settings   settings for theme
     * @param horizontal true if footer is in a horizontal list
     */
    public Footer(@NonNull View v, GlobalSettings settings, boolean horizontal) {
        super(v);
        CardView background = (CardView) v;
        loadCircle = v.findViewById(R.id.placeholder_loading);
        loadBtn = v.findViewById(R.id.placeholder_button);

        background.setCardBackgroundColor(settings.getCardColor());
        loadBtn.setTextColor(settings.getFontColor());
        loadBtn.setTypeface(settings.getTypeFace());
        AppStyles.setButtonColor(loadBtn, settings.getFontColor());
        AppStyles.setProgressColor(loadCircle, settings.getHighlightColor());

        if (horizontal) {
            loadBtn.setVisibility(INVISIBLE);
            loadCircle.setVisibility(VISIBLE);
            background.getLayoutParams().height = MATCH_PARENT;
            background.getLayoutParams().width = WRAP_CONTENT;
        }
    }
}
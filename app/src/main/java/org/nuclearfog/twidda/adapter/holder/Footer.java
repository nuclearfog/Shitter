package org.nuclearfog.twidda.adapter.holder;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

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
public class Footer extends ViewHolder {

    public final ProgressBar loadCircle;
    public final Button loadBtn;

    /**
     * @param parent     Parent view from adapter
     * @param horizontal true if footer orientation is horizontal
     */
    public Footer(ViewGroup parent, boolean horizontal) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_placeholder, parent, false));
        CardView background = (CardView) itemView;
        loadCircle = itemView.findViewById(R.id.placeholder_loading);
        loadBtn = itemView.findViewById(R.id.placeholder_button);

        GlobalSettings settings = GlobalSettings.getInstance(parent.getContext());
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
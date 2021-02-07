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

/**
 * ViewHolder class for a footer/placeholder view
 *
 * @author nuclearfog
 */
public class Footer extends RecyclerView.ViewHolder {

    public final ProgressBar loadCircle;
    public final Button loadBtn;

    public Footer(@NonNull View v, GlobalSettings settings) {
        super(v);
        CardView background = (CardView) v;
        loadCircle = v.findViewById(R.id.placeholder_loading);
        loadBtn = v.findViewById(R.id.placeholder_button);

        background.setCardBackgroundColor(settings.getCardColor());
        loadBtn.setTextColor(settings.getFontColor());
        loadBtn.setTypeface(settings.getFontFace());
        AppStyles.setButtonColor(loadBtn, settings.getFontColor());
        AppStyles.setProgressColor(loadCircle, settings.getHighlightColor());
    }
}